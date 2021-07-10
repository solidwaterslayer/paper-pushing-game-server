package com.manager.transaction.bank;

public class Checking extends Account {
    public Checking(String id, double apr) {
        super(id, apr);
    }

    @Override
    protected double maxDeposit() {
        return 1000;
    }

    @Override
    public boolean isWithdrawValid(double balance) {
        return 0 < balance && balance <= 400;
    }
}
