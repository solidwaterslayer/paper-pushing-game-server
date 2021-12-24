package server.game.pushing.paper.store.bank.account;

public class SavingsAccount extends Account {
    private boolean isMonthlyWithdrawLimit;

    public SavingsAccount(String id) {
        super(AccountType.SAVINGS, id, 0);

        isMonthlyWithdrawLimit = false;
        maxDepositAmount = 2500;
        maxWithdrawAmount = 1000;
    }

    @Override
    public void timeTravel(int months) {
        isMonthlyWithdrawLimit = false;
    }

    @Override
    public void withdraw(double withdrawAmount) {
        super.withdraw(withdrawAmount);
        isMonthlyWithdrawLimit = true;
    }

    @Override
    public boolean isDepositAmountValid(double depositAmount) {
        return minDepositAmount < depositAmount && depositAmount <= maxDepositAmount;
    }

    @Override
    public boolean isWithdrawAmountValid(double withdrawAmount) {
        return !isMonthlyWithdrawLimit && minWithdrawAmount < withdrawAmount && withdrawAmount <= maxWithdrawAmount;
    }
}
