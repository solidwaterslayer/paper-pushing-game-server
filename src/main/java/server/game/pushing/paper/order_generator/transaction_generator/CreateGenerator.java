package server.game.pushing.paper.order_generator.transaction_generator;

import server.game.pushing.paper.store.bank.Bank;
import server.game.pushing.paper.store.bank.account.AccountType;
import server.game.pushing.paper.store.chain_of_responsibility.TransactionType;

import java.util.Random;

public class CreateGenerator extends TransactionGenerator {
    public CreateGenerator(Bank bank, Random random) {
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
            double cdBalance = getRandomAmount(bank.getMaxCDBalance());
            transaction = String.format("%s %s %s %.2f", transactionType, accountType, id, cdBalance).toLowerCase();
        }

        return transaction;
    }

    @Override
    protected String getRandomID() {
        String id = "0000000" + bank.size();
        return id.substring(id.length() - 8);
    }
}
