package com.manager.transaction.bank;

public abstract class Account {
    protected String id;
    protected double apr;
    protected double balance;

    public Account(String id, double apr) {
        this.id = id;
        this.apr = apr;
        balance = 0;
    }

    public String getID() {
        return id;
    }

    public double getAPR() {
        return apr;
    }

    public double getBalance() {
        return balance;
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
        deposit(balance * apr / 100 / 12);
    }

    public boolean isDepositValid(double balance) {
        return 0 < balance && balance <= maxDeposit();
    }

    protected abstract double maxDeposit();

    public abstract boolean isWithdrawValid(double balance);
}
