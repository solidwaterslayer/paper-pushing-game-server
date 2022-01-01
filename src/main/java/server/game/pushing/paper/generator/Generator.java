package server.game.pushing.paper.generator;

import server.game.pushing.paper.TransactionType;
import server.game.pushing.paper.store.bank.AccountType;
import server.game.pushing.paper.store.bank.Bank;
import server.game.pushing.paper.store.handler.ChainOfResponsibility;
import server.game.pushing.paper.store.handler.Handler;

import java.util.Random;

public abstract class Generator {
    protected Random random;
    protected Bank bank;
    protected TransactionType transactionType;
    protected Handler validators;

    public Generator(Random random, Bank bank) {
        this.random = random;
        this.bank = bank;
        validators = (new ChainOfResponsibility(bank)).getValidators();
    }

    protected void checkException() {
        if (bank.getAccounts().stream().filter(id -> bank.getAccount(id).getAccountType() == AccountType.Checking).count() < 2) {
            throw new IllegalArgumentException("the bank contains less than 2 checking accounts");
        }
    }

    public abstract String generateTransaction();

    protected String generateID(boolean isUnique) {
        if (isUnique) {
            String id = String.format("0000000%s", bank.size());
            return id.substring(id.length() - 8);
        }

        return bank.getAccounts().get(random.nextInt(bank.getAccounts().size()));
    }

    protected double generateNumber(double number) {
        return (int) (number * random.nextDouble() / 100) * 100;
    }
}
