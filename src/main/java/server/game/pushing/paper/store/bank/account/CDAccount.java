package server.game.pushing.paper.store.bank.account;

import static server.game.pushing.paper.store.bank.Bank.getMonthsPerYear;

public class CDAccount extends Account {
    public CDAccount(String id, double balance) {
        super(AccountType.CD, id, balance);

        maxWithdrawAmount = Double.POSITIVE_INFINITY;
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
