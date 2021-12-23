package server.game.pushing.paper.store.bank.account;

import static server.game.pushing.paper.store.bank.Bank.getMonthsPerYear;

public class CDAccount extends Account {
    private int lifetime;

    public CDAccount(String id, double balance) {
        super(AccountType.CD, id, balance);

        maxWithdrawAmount = Double.POSITIVE_INFINITY;
        lifetime = 0;
    }

    @Override
    public void timeTravel(int months) {
        lifetime += months;
    }

    @Override
    public boolean isDepositAmountValid(double depositAmount) {
        return false;
    }

    @Override
    public boolean isWithdrawAmountValid(double withdrawAmount) {
        return withdrawAmount >= this.balance && lifetime >= getMonthsPerYear();
    }
}
