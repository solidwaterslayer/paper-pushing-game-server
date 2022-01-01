package server.game.pushing.paper.generator;

import server.game.pushing.paper.store.bank.AccountType;
import server.game.pushing.paper.store.bank.Bank;

import java.util.Random;

import static server.game.pushing.paper.TransactionType.Create;

public class CreateGenerator extends Generator {
    public CreateGenerator(Random random, Bank bank) {
        super(random, bank);
        transactionType = Create;
    }

    public String generateTransaction() {
        return generateTransaction(AccountType.values()[random.nextInt(AccountType.values().length)]);
    }

    public String generateTransaction(AccountType accountType) {
        String transaction = "";
        while (!validators.handleTransaction(transaction)) {
            String id = generateID(true);
            double cdBalance = generateNumber(bank.getMaxCDBalance());

            transaction = String.format("%s %s %s %.2f", transactionType, accountType, id, cdBalance).toLowerCase();
        }

        return transaction;
    }
}
