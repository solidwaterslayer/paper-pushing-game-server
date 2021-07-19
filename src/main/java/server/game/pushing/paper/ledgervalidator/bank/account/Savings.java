package server.game.pushing.paper.ledgervalidator.bank.account;

public class Savings extends Account {
    protected boolean isWithdrawValid;

    public Savings(String id, double apr) {
        super(id, apr);
        accountType = AccountType.Savings;
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
    protected double maxDeposit() {
        return 2500;
    }

    @Override
    public boolean isWithdrawValid(double balance) {
        return isWithdrawValid && 0 < balance && balance <= 1000;
    }
}
