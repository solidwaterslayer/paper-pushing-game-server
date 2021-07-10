package com.manager.transaction.bank;

public class CD extends Account {
    protected int months;

    public CD(String id, double apr, double balance) {
        super(id, apr);
        this.balance = balance;
        months = 0;
    }

    @Override
    public int maxDeposit() {
        return 0;
    }

    @Override
    public boolean isWithdrawValid(double balance) {
        return balance >= this.balance && months >= 12;
    }

    @Override
    public void applyAPR() {
        for (int i = 0; i < 4; i++) {
            super.applyAPR();
        }

        months++;
    }
}
