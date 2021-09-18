package server.game.pushing.paper.ledgervalidator.bank.account;

public class Savings extends Account {
    private boolean isWithdrawValid;

    public Savings(String id, double apr) {
        super(AccountType.Savings, id, apr);
        isWithdrawValid = true;
        maxDepositAmount = 2500;
        maxWithdrawAmount = 1000;
    }

    @Override
    public void withdraw(double withdrawAmount) {
        super.withdraw(withdrawAmount);
        isWithdrawValid = false;
    }

    @Override
    public void applyAPR() {
        super.applyAPR();
        isWithdrawValid = true;
    }

    @Override
    public boolean isDepositAmountValid(double depositAmount) {
        return minDepositAmount < depositAmount && depositAmount <= maxDepositAmount;
    }

    @Override
    public boolean isWithdrawAmountValid(double withdrawAmount) {
        return isWithdrawValid && minWithdrawAmount < withdrawAmount && withdrawAmount <= maxWithdrawAmount;
    }
}
