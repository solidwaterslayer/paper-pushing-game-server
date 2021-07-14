package server.game.pushing.paper.validator.transaction;

import server.game.pushing.paper.bank.Bank;

public abstract class TransactionValidator {
    protected TransactionValidator nextHandler;
    protected Bank bank;

    public TransactionValidator(TransactionValidator nextHandler, Bank bank) {
        this.nextHandler = nextHandler;
        this.bank = bank;
    }

    public abstract boolean isTransactionValid(String[] transactionArguments);
}
