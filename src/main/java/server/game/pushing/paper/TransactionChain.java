package server.game.pushing.paper;

import server.game.pushing.paper.bank.Bank;

import java.util.List;

public abstract class TransactionChain {
    protected TransactionChain next;
    protected final Bank bank;

    public TransactionChain(Bank bank) {
        this.bank = bank;
    }

    public static TransactionChain createTransactionChain(List<TransactionChain> transactionChains) {
        int i = 0;
        TransactionChain transactionChain = transactionChains.get(i);
        while (transactionChain != null) {
            transactionChain.setNext(transactionChains.get(i + 1));

            i++;
            transactionChain = transactionChains.get(i);
        }

        return transactionChains.get(0);
    }

    public void setNext(TransactionChain next) {
        this.next = next;
    }

    public boolean handle(String transaction) {
        return handle(transaction.split(" "));
    }

    public abstract boolean handle(String[] transactionArguments);
}