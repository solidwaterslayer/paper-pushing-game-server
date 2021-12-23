package server.game.pushing.paper.store.bank;

import server.game.pushing.paper.store.bank.account.Account;
import server.game.pushing.paper.store.bank.account.CDAccount;
import server.game.pushing.paper.store.bank.account.CheckingAccount;
import server.game.pushing.paper.store.bank.account.SavingsAccount;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Math.min;

public class Bank {
    private final double MIN_BALANCE_FEE;
    private final Map<String, Account> ACCOUNTS;

    private final double MIN_STARTING_CD_BALANCE;
    private final double MAX_STARTING_CD_BALANCE;
    private final int MAX_MONTHS;

    public Bank() {
        MIN_BALANCE_FEE = 100;
        this.ACCOUNTS = new LinkedHashMap<>();

        MIN_STARTING_CD_BALANCE = 1000;
        MAX_STARTING_CD_BALANCE = 10000;
        MAX_MONTHS = 60;
    }

    public double getMinBalanceFee() {
        return MIN_BALANCE_FEE;
    }

    public void createChecking(String id) {
        ACCOUNTS.put(id, new CheckingAccount(id));
    }

    public void createSavings(String id) {
        ACCOUNTS.put(id, new SavingsAccount(id));
    }

    public void createCD(String id, double balance) {
        ACCOUNTS.put(id, new CDAccount(id, balance));
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

    public List<String> getIDs() {
        return new ArrayList<>(ACCOUNTS.keySet());
    }

    public void deposit(String id, double depositAmount) {
        getAccount(id).deposit(depositAmount);
    }

    public void withdraw(String id, double withdrawAmount) {
        getAccount(id).withdraw(withdrawAmount);
    }

    public void transfer(String payingID, String receivingID, double transferAmount) {
        Account payingAccount = getAccount(payingID);
        Account receivingAccount = getAccount(receivingID);
        transferAmount = min(transferAmount, payingAccount.getBalance());

        payingAccount.withdraw(transferAmount);
        receivingAccount.deposit(transferAmount);
    }

    public void timeTravel(int months) {
        for (Account account : new ArrayList<>(ACCOUNTS.values())) {
            if (isLowBalance(account.getBalance())) {
                account.withdraw(MIN_BALANCE_FEE * months);
            }

            account.timeTravel(months);
        }
    }

    public boolean isLowBalance(double balance) {
        return balance <= 900;
    }

    public boolean isIDValid(String id) {
        return !containsAccount(id) && id.matches("[0-9]{8}");
    }

    public boolean isStartingCDBalanceValid(double startingCDBalance) {
        return MIN_STARTING_CD_BALANCE <= startingCDBalance && startingCDBalance <= MAX_STARTING_CD_BALANCE;
    }

    public double getMinStartingCDBalance() {
        return MIN_STARTING_CD_BALANCE;
    }

    public double getMaxStartingCDBalance() {
        return MAX_STARTING_CD_BALANCE;
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

    public boolean isTimeTravelValid(int months) {
        return 1 <= months && months <= MAX_MONTHS;
    }

    public static int getMonthsPerYear() {
        return 12;
    }

    public int getMaxMonths() {
        return MAX_MONTHS;
    }
}
