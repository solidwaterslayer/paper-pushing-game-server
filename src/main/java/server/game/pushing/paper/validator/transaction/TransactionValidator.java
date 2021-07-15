package server.game.pushing.paper.validator.transaction;

import server.game.pushing.paper.bank.Bank;

public abstract class TransactionValidator {
    protected final TransactionValidator nextHandler;
    protected final Bank bank;

    public TransactionValidator(TransactionValidator nextHandler, Bank bank) {
        this.nextHandler = nextHandler;
        this.bank = bank;
    }

    public boolean isTransactionValid(String transaction) {
        return isTransactionValid(transaction.split(" "));
    }

    protected abstract boolean isTransactionValid(String[] transactionArguments);
}
