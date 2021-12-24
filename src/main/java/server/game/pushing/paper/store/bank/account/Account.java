package server.game.pushing.paper.store.bank.account;

import static java.lang.Math.max;

public abstract class Account {
    protected AccountType accountType;
    protected final String id;
    protected double balance;

    protected double minDepositAmount;
    protected double maxDepositAmount;
    protected double minWithdrawAmount;
    protected double maxWithdrawAmount;

    protected Account(AccountType accountType, String id, double balance) {
        this.accountType = accountType;
        this.id = id;
        this.balance = balance;

        minDepositAmount = 0;
        minWithdrawAmount = 0;
    }

    @Override
    public String toString() {
        return String.format("%s %s %.2f", accountType, id, balance).toLowerCase();
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public String getID() {
        return id;
    }

    public double getBalance() {
        return balance;
    }

    public void timeTravel(int months) {}

    public void deposit(double depositAmount) {
        this.balance += depositAmount;
    }

    public void withdraw(double withdrawAmount) {
        this.balance = max(0, this.balance - withdrawAmount);
    }

    public abstract boolean isDepositAmountValid(double depositAmount);

    public abstract boolean isWithdrawAmountValid(double withdrawAmount);

    public double getMaxDepositAmount() {
        return maxDepositAmount;
    }

    public double getMaxWithdrawAmount() {
        return maxWithdrawAmount;
    }
}
