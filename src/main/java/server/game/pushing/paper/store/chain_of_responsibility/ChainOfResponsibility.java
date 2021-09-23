package server.game.pushing.paper.store.chain_of_responsibility;

import server.game.pushing.paper.store.bank.Bank;

public abstract class ChainOfResponsibility {
    protected Bank bank;
    protected ChainOfResponsibility next;

    protected TransactionType transactionType;

    public ChainOfResponsibility(Bank bank) {
        this.bank = bank;
    }

    public void setNext(ChainOfResponsibility next) {
        this.next = next;
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public boolean handle(String transaction) {
        return handle(transaction.split(" "));
    }

    public abstract boolean handle(String[] transactionArguments);

    public static double parseDouble(String string) {
        if (string.equalsIgnoreCase("infinity")) {
            return Double.POSITIVE_INFINITY;
        }

        return Double.parseDouble(string);
    }
}
