package server.game.pushing.paper.ledgervalidator.bank.account;

import static server.game.pushing.paper.ledgervalidator.bank.Bank.getMonthsPerYear;

public abstract class Account {
    protected AccountType accountType;
    protected final String ID;
    protected final double APR;
    protected double balance;

    protected Account(AccountType accountType, String id, double apr) {
        this.accountType = accountType;
        this.ID = id;
        this.APR = apr;
        balance = 0;
    }

    protected Account(AccountType accountType, String id, double apr, double balance) {
        this.accountType = accountType;
        this.ID = id;
        this.APR = apr;
        this.balance = balance;
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

    @Override
    public String toString() {
        return String.format("%s %s %.2f %.2f", accountType, ID, APR, balance).toLowerCase();
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
        deposit(balance * APR / getMonthsPerYear() / 100);
    }

    public abstract boolean isDepositValid(double depositAmount);

    public abstract boolean isWithdrawValid(double withdrawAmount);
}
