package server.game.pushing.paper.order_factory.transaction_factory;

import server.game.pushing.paper.store.bank.Bank;
import server.game.pushing.paper.store.bank.account.AccountType;
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
        validator = (new ChainOfResponsibilityFactory(bank)).getChainOfResponsibility(true);
    }

    public abstract String getTransaction();

    protected String getRandomID() {
        return bank.getIDs().get(random.nextInt(bank.getIDs().size()));
    }

    protected double getRandomAmount(double amount) {
        return (int) (amount * random.nextDouble() / 100) * 100;
    }

    protected void checkException() {
        if (bank.getIDs().stream().filter(id -> bank.getAccount(id).getAccountType() == AccountType.CHECKING).count() < 2 && transactionType != TransactionType.Create) {
            throw new IllegalArgumentException("the bank contains less than 2 checking accounts");
        }
        if (bank.size() >= 1000) {
            throw new IllegalArgumentException("create factories can not support more than 1000 create transactions");
        }
    }
}
