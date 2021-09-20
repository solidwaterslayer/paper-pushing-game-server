package server.game.pushing.paper.store.bank;

import server.game.pushing.paper.store.bank.account.Account;
import server.game.pushing.paper.store.bank.account.CD;
import server.game.pushing.paper.store.bank.account.Checking;
import server.game.pushing.paper.store.bank.account.Savings;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

public class Bank {
    private final Map<String, Account> ACCOUNTS;
    private final double MIN_BALANCE_FEE;

    private final String VALID_ID;
    private final double MIN_APR;
    private final double MAX_APR;
    private final double MIN_INITIAL_CD_BALANCE;
    private final double MAX_INITIAL_CD_BALANCE;

    public Bank() {
        this.ACCOUNTS = new LinkedHashMap<>();
        MIN_BALANCE_FEE = 25;

        VALID_ID = "[0-9]{8}";
        MIN_APR = 0;
        MAX_APR = 10;
        MIN_INITIAL_CD_BALANCE = 1000;
        MAX_INITIAL_CD_BALANCE = 10000;
    }

    public void createChecking(String id, double apr) {
        ACCOUNTS.put(id, new Checking(id, apr));
    }

    public void createSavings(String id, double apr) {
        ACCOUNTS.put(id, new Savings(id, apr));
    }

    public void createCD(String id, double apr, double balance) {
        ACCOUNTS.put(id, new CD(id, apr, balance));
    }

    public Account getAccount(String id) {
        return ACCOUNTS.get(id);
    }

    public void removeAccount(String id) {
        ACCOUNTS.remove(id);
    }

    public boolean isEmpty() {
        return ACCOUNTS.isEmpty();
    }

    public int size() {
        return ACCOUNTS.size();
    }

    public boolean containsAccount(String id) {
        return ACCOUNTS.containsKey(id);
    }

    public Iterator<String> getAccountIterator() {
        return ACCOUNTS.keySet().iterator();
    }

    public double getMinBalanceFee() {
        return MIN_BALANCE_FEE;
    }

    public void deposit(String id, double depositAmount) {
        getAccount(id).deposit(depositAmount);
    }

    public void withdraw(String id, double withdrawAmount) {
        getAccount(id).withdraw(withdrawAmount);
    }

    public static int getMonthsPerYear() {
        return 12;
    }

    public void transfer(String fromID, String toID, double transferAmount) {
        Account fromAccount = getAccount(fromID);
        Account toAccount = getAccount(toID);

        if (transferAmount > fromAccount.getBalance()) {
            transfer(fromID, toID, fromAccount.getBalance());
            return;
        }

        fromAccount.withdraw(transferAmount);
        toAccount.deposit(transferAmount);
    }

    public void passTime(int months) {
        for (int i = 0; i < months; i++) {
            for (Account account : new ArrayList<>(ACCOUNTS.values())) {
                if (account.getBalance() == 0) {
                    ACCOUNTS.remove(account.getID());
                } else if (account.getBalance() <= 100) {
                    account.withdraw(MIN_BALANCE_FEE);
                }

                account.applyAPR();
            }
        }
    }

    public boolean isIDValid(String id) {
        return !containsAccount(id) && id.matches(VALID_ID);
    }

    public boolean isAPRValid(double apr) {
        return MIN_APR <= apr && apr <= MAX_APR;
    }

    public double getMaxAPR() {
        return MAX_APR;
    }

    public boolean isInitialCDBalanceValid(double initialCDBalance) {
        return MIN_INITIAL_CD_BALANCE <= initialCDBalance && initialCDBalance <= MAX_INITIAL_CD_BALANCE;
    }

    public double getMinInitialCDBalance() {
        return MIN_INITIAL_CD_BALANCE;
    }

    public double getMaxInitialCDBalance() {
        return MAX_INITIAL_CD_BALANCE;
    }

    public boolean isDepositAmountValid(String id, double depositAmount) {
        return containsAccount(id) && getAccount(id).isDepositAmountValid(depositAmount);
    }

    public boolean isWithdrawAmountValid(String id, double withdrawAmount) {
        return containsAccount(id) && getAccount(id).isWithdrawAmountValid(withdrawAmount);
    }

    public boolean isTransferAmountValid(String fromID, String toID, double transferAmount) {
        return !fromID.equals(toID) && isWithdrawAmountValid(fromID, transferAmount) && isDepositAmountValid(toID, transferAmount);
    }

    public boolean isPassTimeValid(int months) {
        return 1 <= months && months <= 60;
    }
}
