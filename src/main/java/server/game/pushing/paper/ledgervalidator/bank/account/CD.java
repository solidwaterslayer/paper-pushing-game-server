package server.game.pushing.paper.ledgervalidator.bank.account;

import static server.game.pushing.paper.ledgervalidator.bank.Bank.getMonthsPerYear;

public class CD extends Account {
    protected int months;

    public CD(String id, double apr, double balance) {
        super(AccountType.CD, id, apr, balance);
        months = 0;
        // TODO: test maxDepositAmount = 0.0d
        maxDepositAmount = 0;
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
        return minDepositAmount < depositAmount && depositAmount <= maxDepositAmount;
    }

    @Override
    public boolean isWithdrawAmountValid(double withdrawAmount) {
        return months >= getMonthsPerYear() && withdrawAmount >= this.balance;
    }
}
