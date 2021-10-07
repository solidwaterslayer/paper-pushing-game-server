package server.game.pushing.paper.order_factory.transaction_factory;

import server.game.pushing.paper.store.bank.Bank;
import server.game.pushing.paper.store.bank.account.AccountType;
import server.game.pushing.paper.store.chain_of_responsibility.TransactionType;

import java.util.Random;

public class CreateFactory extends TransactionFactory {
    public CreateFactory(Bank bank, Random random) {
        super(bank, random);

        transactionType = TransactionType.Create;
    }

    public String getLoadedTransaction(AccountType accountType) {
        return getTransaction(accountType);
    }

    public String getTransaction() {
        return getTransaction(AccountType.values()[random.nextInt(AccountType.values().length)]);
    }

    private String getTransaction(AccountType accountType) {
        checkException();

        String transaction = "";
        while (!validator.handle(transaction)) {
            String id = getRandomID();
            double initialCDBalance = getRandomAmount(bank.getMaxInitialCDBalance());
            transaction = String.format("%s %s %s %.2f", transactionType, accountType, id, initialCDBalance).toLowerCase();
        }

        return transaction;
    }

    @Override
    protected String getRandomID() {
        String id = "0000000" + bank.size();
        return id.substring(id.length() - 8);
    }
}
