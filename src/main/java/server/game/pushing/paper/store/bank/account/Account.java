package server.game.pushing.paper.store.bank.account;

import static server.game.pushing.paper.store.bank.Bank.getMonthsPerYear;

public abstract class Account {
    protected AccountType accountType;
    protected final String ID;
    protected final double APR;
    protected double balance;
    protected double minDepositAmount;
    protected double maxDepositAmount;
    protected double minWithdrawAmount;
    protected double maxWithdrawAmount;

    protected Account(AccountType accountType, String id, double apr) {
        this.accountType = accountType;
        this.ID = id;
        this.APR = apr;
        balance = 0;
        minDepositAmount = 0;
        minWithdrawAmount = 0;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public String getID() {
        return ID;
    }

    public double getAPR() {
        return APR;
    }

    public double getBalance() {
        return balance;
    }

    public double getMaxDepositAmount() {
        return maxDepositAmount;
    }

    public double getMaxWithdrawAmount() {
        return maxWithdrawAmount;
    }

    @Override
    public String toString() {
        return String.format("%s %s %.2f %.2f", accountType, ID, APR, balance).toLowerCase();
    }

    public void deposit(double depositAmount) {
        this.balance += depositAmount;
    }

    public void withdraw(double withdrawAmount) {
        if (withdrawAmount > this.balance) {
            withdraw(this.balance);
            return;
        }

        this.balance -= withdrawAmount;
    }

    public void applyAPR() {
        deposit(APR * balance / getMonthsPerYear() / 100);
    }

    public abstract boolean isDepositAmountValid(double depositAmount);

    public abstract boolean isWithdrawAmountValid(double withdrawAmount);
}
