package server.game.pushing.paper.bank.account;

public abstract class Account {
    protected AccountType accountType;
    protected final String ID;
    protected final double APR;
    protected double balance;

    public Account(String id, double apr) {
        this.ID = id;
        this.APR = apr;
        balance = 0;
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public String getID() {
        return ID;
    }

    public double getAPR() {
        return APR;
    }

    public double getBalance() {
        return balance;
    }

    public void deposit(double depositAmount) {
        this.balance += depositAmount;
    }

    public void withdraw(double withdrawAmount) {
        if (withdrawAmount > this.balance) {
            withdraw(this.balance);
            return;
        }

        this.balance -= withdrawAmount;
    }

    public void applyAPR() {
        deposit(balance * APR / 100 / 12);
    }

    public boolean isDepositValid(double balance) {
        return 0 < balance && balance <= maxDeposit();
    }

    protected abstract double maxDeposit();

    public abstract boolean isWithdrawValid(double balance);
}
