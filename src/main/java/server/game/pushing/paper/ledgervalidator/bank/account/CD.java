package server.game.pushing.paper.ledgervalidator.bank.account;

import static server.game.pushing.paper.ledgervalidator.bank.Bank.getMonthsPerYear;

public class CD extends Account {
    protected int months;

    public CD(String id, double apr, double balance) {
        super(AccountType.CD, id, apr, balance);
        months = 0;
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
        return 0 < depositAmount && depositAmount <= getMaxDepositAmount();
    }

    public static double getMaxDepositAmount() {
        return 0.0d;
    }

    @Override
    public boolean isWithdrawAmountValid(double withdrawAmount) {
        return months >= getMonthsPerYear() && withdrawAmount >= this.balance;
    }

    public static double getMaxWithdrawAmount() {
        return Double.POSITIVE_INFINITY;
    }
}
