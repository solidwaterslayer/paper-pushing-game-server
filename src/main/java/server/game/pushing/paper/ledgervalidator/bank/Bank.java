package server.game.pushing.paper.ledgervalidator.bank;

import server.game.pushing.paper.ledgervalidator.bank.account.Account;
import server.game.pushing.paper.ledgervalidator.bank.account.CD;
import server.game.pushing.paper.ledgervalidator.bank.account.Checking;
import server.game.pushing.paper.ledgervalidator.bank.account.Savings;

import java.util.*;

public class Bank {
    protected Map<String, Account> accounts;
    protected double minBalanceFee;

    public Bank(List<Account> accounts) {
        initializeBank();

        for (Account account : accounts) {
            this.accounts.put(account.getID(), account);
        }
    }

    public Bank() {
        initializeBank();
    }

    protected void initializeBank() {
        this.accounts = new LinkedHashMap<>();
        minBalanceFee = 25;
    }

    public void createChecking(String id, double apr) {
        accounts.put(id, new Checking(id, apr));
    }

    public void createSavings(String id, double apr) {
        accounts.put(id, new Savings(id, apr));
    }

    public void createCD(String id, double apr, double balance) {
        accounts.put(id, new CD(id, apr, balance));
    }

    public Map<String, Account> getAccounts() {
        return accounts;
    }

    public Account getAccount(String id) {
        return accounts.get(id);
    }

    public void removeAccount(String id) {
        accounts.remove(id);
    }

    public boolean containsAccount(String id) {
        return accounts.containsKey(id);
    }

    public double getMinBalanceFee() {
        return minBalanceFee;
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
            for (Account account : new ArrayList<>(accounts.values())) {
                if (account.getBalance() == 0) {
                    accounts.remove(account.getID());
                } else if (account.getBalance() <= 100) {
                    account.withdraw(minBalanceFee);
                }

                account.applyAPR();
            }
        }
    }

    public boolean isIDValid(String id) {
        return !containsAccount(id) && id.matches("[0-9]{8}");
    }

    public boolean isAPRValid(double apr) {
        return 0 <= apr && apr <= getMaxAPR();
    }

    public static double getMaxAPR() {
        return 10;
    }

    public boolean isInitialCDBalanceValid(double balance) {
        return getMinInitialCDBalance() <= balance && balance <= 10000;
    }

    public static double getMinInitialCDBalance() {
        return 1000;
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
