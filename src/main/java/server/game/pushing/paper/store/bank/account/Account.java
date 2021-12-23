package server.game.pushing.paper.store.bank.account;

import static java.lang.Math.max;

public abstract class Account {
    protected int lifetime;
    protected AccountType accountType;
    protected final String ID;
    protected double balance;

    protected double minDepositAmount;
    protected double maxDepositAmount;
    protected double minWithdrawAmount;
    protected double maxWithdrawAmount;

    protected Account(AccountType accountType, String id, double balance) {
        lifetime = 0;
        this.accountType = accountType;
        this.ID = id;
        this.balance = balance;

        minDepositAmount = 0;
        minWithdrawAmount = 0;
    }

    @Override
    public String toString() {
        return String.format("%s %s %.2f", accountType, ID, balance).toLowerCase();
    }

    public int getLifetime() {
        return lifetime;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public String getID() {
        return ID;
    }

    public double getBalance() {
        return balance;
    }

    public void deposit(double depositAmount) {
        this.balance += depositAmount;
    }

    public void withdraw(double withdrawAmount) {
        this.balance = max(0, this.balance - withdrawAmount);
    }

    public void timeTravel(int months) {
        this.lifetime += months;
    }

    public double getMaxDepositAmount() {
        return maxDepositAmount;
    }

    public double getMaxWithdrawAmount() {
        return maxWithdrawAmount;
    }

    public abstract boolean isDepositAmountValid(double depositAmount);

    public abstract boolean isWithdrawAmountValid(double withdrawAmount);
}
