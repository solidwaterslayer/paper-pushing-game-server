package server.game.pushing.paper.ledgervalidator.bank.account;

public class Checking extends Account {
    public Checking(String id, double apr) {
        super(AccountType.Checking, id, apr);
        maxDepositAmount = 1000;
        maxWithdrawAmount = 400;
    }

    @Override
    public boolean isDepositAmountValid(double depositAmount) {
        return minDepositAmount < depositAmount && depositAmount <= maxDepositAmount;
    }

    @Override
    public boolean isWithdrawAmountValid(double withdrawAmount) {
        return minWithdrawAmount < withdrawAmount && withdrawAmount <= maxWithdrawAmount;
    }
}
