package server.game.pushing.paper.store.bank.account;

import static server.game.pushing.paper.store.bank.Bank.getMonthsPerYear;

public class CD extends Account {
    private int months;

    public CD(String id, double apr, double balance) {
        super(AccountType.CD, id, apr);
        months = 0;
        this.balance = balance;
        maxWithdrawAmount = Double.POSITIVE_INFINITY;
    }

    @Override
    public void applyAPR() {
        super.applyAPR();
        super.applyAPR();
        super.applyAPR();
        super.applyAPR();
        months++;
    }

    @Override
    public boolean isDepositAmountValid(double depositAmount) {
        return false;
    }

    @Override
    public boolean isWithdrawAmountValid(double withdrawAmount) {
        return months >= getMonthsPerYear() && withdrawAmount >= this.balance;
    }
}
