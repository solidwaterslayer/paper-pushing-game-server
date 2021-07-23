package server.game.pushing.paper.ledgervalidator.bank.account;

public class Savings extends Account {
    protected boolean isWithdrawValid;

    public Savings(String id, double apr) {
        super(AccountType.Savings, id, apr);
        isWithdrawValid = true;
    }

    @Override
    public void withdraw(double withdrawAmount) {
        super.withdraw(withdrawAmount);
        isWithdrawValid = false;
    }

    @Override
    public void applyAPR() {
        super.applyAPR();
        isWithdrawValid = true;
    }

    @Override
    public boolean isDepositAmountValid(double depositAmount) {
        return 0 < depositAmount && depositAmount <= getMaxDepositAmount();
    }

    public static double getMaxDepositAmount() {
        return 2500;
    }

    @Override
    public boolean isWithdrawAmountValid(double withdrawAmount) {
        return isWithdrawValid && 0 < withdrawAmount && withdrawAmount <= getMaxWithdrawAmount();
    }

    public static double getMaxWithdrawAmount() {
        return 1000;
    }
}
