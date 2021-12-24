package server.game.pushing.paper.store.bank.account;

public class CheckingAccount extends Account {
    public CheckingAccount(String id) {
        super(AccountType.CHECKING, id, 0);

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