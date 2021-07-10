package com.manager.transaction.bank;

public class Savings extends Account {
    protected boolean isWithdrawValid;

    public Savings(String id, double apr) {
        super(id, apr);
        isWithdrawValid = true;
    }

    @Override
    public void withdraw(double withdrawAmount) {
        super.withdraw(withdrawAmount);
        isWithdrawValid = false;
    }

    @Override
    public int maxDeposit() {
        return 2500;
    }

    @Override
    public boolean isWithdrawValid(double balance) {
        return 0 <= balance && balance <= 1000 && isWithdrawValid;
    }

    @Override
    public void applyAPR() {
        super.applyAPR();
        isWithdrawValid = true;
    }
}
