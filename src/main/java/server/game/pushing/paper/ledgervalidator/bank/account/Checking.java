package server.game.pushing.paper.ledgervalidator.bank.account;

public class Checking extends Account {
    public Checking(String id, double apr) {
        super(AccountType.Checking, id, apr);
    }

    @Override
    public boolean isDepositValid(double depositAmount) {
        return 0 < depositAmount && depositAmount <= getMaxDepositAmount();
    }

    public static double getMaxDepositAmount() {
        return 1000;
    }

    @Override
    public boolean isWithdrawValid(double withdrawAmount) {
        return 0 < withdrawAmount && withdrawAmount <= getMaxWithdrawAmount();
    }

    public static double getMaxWithdrawAmount() {
        return 400;
    }
}
