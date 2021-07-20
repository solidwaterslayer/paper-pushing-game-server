package server.game.pushing.paper.ledgervalidator.bank.account;

public class Checking extends Account {
    public Checking(String id, double apr) {
        super(id, apr);
        accountType = AccountType.Checking;
    }

    @Override
    public double maxDeposit() {
        return 1000;
    }

    @Override
    public boolean isWithdrawValid(double balance) {
        return 0 < balance && balance <= 400;
    }
}
