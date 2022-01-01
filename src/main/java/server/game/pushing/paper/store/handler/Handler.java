package server.game.pushing.paper.store.handler;

import server.game.pushing.paper.store.bank.Bank;

public abstract class Handler {
    protected Bank bank;
    protected TransactionType transactionType;
    protected Handler next;

    public Handler(Bank bank) {
        this.bank = bank;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public void setNext(Handler next) {
        this.next = next;
    }

    public boolean handleTransaction(String transaction) {
        return handleTransaction(transaction.split(" "));
    }

    public abstract boolean handleTransaction(String[] transactionArguments);
}
