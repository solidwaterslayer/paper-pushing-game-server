package server.game.pushing.paper;

import server.game.pushing.paper.bank.Bank;

public abstract class TransactionHandler {
    protected final TransactionHandler nextHandler;
    protected final Bank bank;

    public TransactionHandler(TransactionHandler nextHandler, Bank bank) {
        this.nextHandler = nextHandler;
        this.bank = bank;
    }

    public boolean handle(String transaction) {
        return handle(transaction.split(" "));
    }

    public abstract boolean handle(String[] transactionArguments);
}
