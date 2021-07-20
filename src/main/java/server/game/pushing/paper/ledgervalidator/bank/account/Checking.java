package server.game.pushing.paper.ledgervalidator.bank.account;

public class Checking extends Account {
    public Checking(String id, double apr) {
        super(AccountType.Checking, id, apr);
    }

    @Override
    public boolean isDepositValid(double depositAmount) {
        return 0 < depositAmount && depositAmount <= getMaxDeposit();
    }

    public static double getMaxDeposit() {
        return 1000;
    }

    @Override
    public boolean isWithdrawValid(double withdrawAmount) {
        return 0 < withdrawAmount && withdrawAmount <= getMaxWithdraw();
    }

    public static double getMaxWithdraw() {
        return 400;
    }
}
