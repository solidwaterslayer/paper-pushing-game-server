package server.game.pushing.paper.store.bank.account;

import server.game.pushing.paper.store.bank.AccountType;

import static server.game.pushing.paper.store.bank.Bank.getMonthsPerYear;

public class CDAccount extends Account {
    private int lifetime;

    public CDAccount(String id, double balance) {
        super(AccountType.CD, id, balance);

        lifetime = 0;
        maxWithdrawAmount = Double.POSITIVE_INFINITY;
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
        return lifetime >= getMonthsPerYear() && withdrawAmount >= this.balance;
    }
}
