package server.game.pushing.paper.bank.account;

public class CD extends Account {
    protected int months;

    public CD(String id, double apr, double balance) {
        super(id, apr);
        accountType = AccountType.CD;
        this.balance = balance;
        months = 0;
    }

    @Override
    public void applyAPR() {
        for (int i = 0; i < 4; i++) {
            super.applyAPR();
        }

        months++;
    }

    @Override
    protected double maxDeposit() {
        return 0.0d;
    }

    @Override
    public boolean isWithdrawValid(double balance) {
        return months >= 12 && balance >= this.balance;
    }
}
