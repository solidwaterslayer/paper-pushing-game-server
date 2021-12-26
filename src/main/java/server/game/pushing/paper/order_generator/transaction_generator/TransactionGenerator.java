package server.game.pushing.paper.order_generator.transaction_generator;

import server.game.pushing.paper.store.bank.AccountType;
import server.game.pushing.paper.store.bank.Bank;
import server.game.pushing.paper.store.handler.ChainOfResponsibility;
import server.game.pushing.paper.store.handler.Handler;
import server.game.pushing.paper.store.handler.TransactionType;

import java.util.Random;

public abstract class TransactionGenerator {
    protected Bank bank;
    protected Random random;
    protected Handler validator;

    protected TransactionType transactionType;

    public TransactionGenerator(Bank bank, Random random) {
        this.bank = bank;
        this.random = random;
        validator = (new ChainOfResponsibility(bank)).getValidator();
    }

    public abstract String getTransaction();

    protected String getRandomID() {
        return bank.getAccounts().get(random.nextInt(bank.getAccounts().size()));
    }

    protected double getRandomAmount(double amount) {
        return (int) (amount * random.nextDouble() / 100) * 100;
    }

    protected void checkException() {
        if (bank.getAccounts().stream().filter(id -> bank.getAccount(id).getAccountType() == AccountType.Checking).count() < 2 && transactionType != TransactionType.Create) {
            throw new IllegalArgumentException("the bank contains less than 2 checking accounts");
        }
        if (bank.size() >= 1000) {
            throw new IllegalArgumentException("create factories can not support more than 1000 create transactions");
        }
    }
}
