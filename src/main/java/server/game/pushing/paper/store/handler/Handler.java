package server.game.pushing.paper.store.handler;

import server.game.pushing.paper.store.bank.Bank;

public abstract class Handler {
    protected TransactionType transactionType;
    protected Bank bank;
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

    public static double parseDouble(String string) {
        if (string.equalsIgnoreCase("infinity")) {
            return Double.POSITIVE_INFINITY;
        }

        return Double.parseDouble(string);
    }
}
