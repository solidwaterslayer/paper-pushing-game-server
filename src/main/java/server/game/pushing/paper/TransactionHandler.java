package server.game.pushing.paper;

import server.game.pushing.paper.bank.Bank;

public abstract class TransactionHandler {
    protected TransactionHandler nextHandler;
    protected final Bank bank;

    public TransactionHandler(Bank bank) {
        this.bank = bank;
    }

    public void setNextHandler(TransactionHandler nextHandler) {
        this.nextHandler = nextHandler;
    }

    public boolean handle(String transaction) {
        return handle(transaction.split(" "));
    }

    public abstract boolean handle(String[] transactionArguments);
}
