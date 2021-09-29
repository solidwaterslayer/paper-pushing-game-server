package server.game.pushing.paper.order_factory.transaction_factory;

import server.game.pushing.paper.store.bank.Bank;
import server.game.pushing.paper.store.chain_of_responsibility.ChainOfResponsibility;
import server.game.pushing.paper.store.chain_of_responsibility.ChainOfResponsibilityFactory;
import server.game.pushing.paper.store.chain_of_responsibility.TransactionType;

import java.util.Random;

public abstract class TransactionFactory {
    protected Bank bank;
    protected Random random;
    protected ChainOfResponsibility validator;

    protected TransactionType transactionType;

    public TransactionFactory(Bank bank, Random random) {
        this.bank = bank;
        this.random = random;
        ChainOfResponsibilityFactory chainOfResponsibilityFactory = new ChainOfResponsibilityFactory(bank);
        validator = chainOfResponsibilityFactory.getChainOfResponsibility(true);
    }

    public abstract String getTransaction();
}
