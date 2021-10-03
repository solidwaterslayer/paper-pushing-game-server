package server.game.pushing.paper.store.bank.account;

public class Savings extends Account {
    private boolean monthlyWithdrawLimit;

    public Savings(String id, double apr) {
        super(AccountType.SAVINGS, id, apr, 0);
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
        super.timeTravel(months);
        monthlyWithdrawLimit = false;
    }

    @Override
    public boolean isDepositAmountValid(double depositAmount) {
        return minDepositAmount < depositAmount && depositAmount <= maxDepositAmount;
    }

    @Override
    public boolean isWithdrawAmountValid(double withdrawAmount) {
        return !monthlyWithdrawLimit && minWithdrawAmount < withdrawAmount && withdrawAmount <= maxWithdrawAmount;
    }
}
