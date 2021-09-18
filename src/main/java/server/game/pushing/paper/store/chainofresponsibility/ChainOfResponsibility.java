package server.game.pushing.paper.store.chainofresponsibility;

import server.game.pushing.paper.store.bank.Bank;

import java.util.List;

public abstract class ChainOfResponsibility {
    protected ChainOfResponsibility next;

    protected Bank bank;
    protected TransactionType transactionType;

    public ChainOfResponsibility(Bank bank) {
        this.bank = bank;
    }

    public static ChainOfResponsibility getInstance(List<ChainOfResponsibility> chainOfResponsibilities) {
        for (int i = 0; i < chainOfResponsibilities.size() - 1; i++) {
            chainOfResponsibilities.get(i).next = chainOfResponsibilities.get(i + 1);
        }

        return chainOfResponsibilities.get(0);
    }

    public TransactionType getTransactionType() {
        return transactionType;
    }

    public boolean handle(String transaction) {
        return handle(transaction.split(" "));
    }

    public abstract boolean handle(String[] transactionArguments);
}
