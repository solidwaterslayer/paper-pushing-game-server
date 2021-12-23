package server.game.pushing.paper.store.bank.account;

public class SavingsAccount extends Account {
    private boolean monthlyWithdrawLimit;

    public SavingsAccount(String id) {
        super(AccountType.SAVINGS, id, 0);

        maxDepositAmount = 2500;
        maxWithdrawAmount = 1000;
        monthlyWithdrawLimit = false;
    }

    @Override
    public void withdraw(double withdrawAmount) {
        super.withdraw(withdrawAmount);
        monthlyWithdrawLimit = true;
    }

    @Override
    public void timeTravel(int months) {
        monthlyWithdrawLimit = false;
    }

    @Override
    public boolean isDepositAmountValid(double depositAmount) {
        return minDepositAmount < depositAmount && depositAmount <= maxDepositAmount;
    }

    @Override
    public boolean isWithdrawAmountValid(double withdrawAmount) {
        return minWithdrawAmount < withdrawAmount && withdrawAmount <= maxWithdrawAmount && !monthlyWithdrawLimit;
    }
}
