package com.manager.transaction.bank;

public abstract class Account {
    String id;
    double apr;
    double balance;

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

    public void deposit(double balance) {
        this.balance += balance;
    }

    public void withdraw(double balance) {
        if (balance > this.balance) {
            withdraw(balance);
            return;
        }

        this.balance -= balance;
    }

    public boolean isDepositValid(double balance) {
        return (0 <= balance && balance <= maxDeposit());
    }

    abstract int maxDeposit();

    abstract boolean isWithdrawValid(double balance);

    public void applyAPR() {
        deposit(balance * apr / 100 / 12);
    }

    public void applyMinimumBalanceFee() {
        withdraw(25);
    }
}
