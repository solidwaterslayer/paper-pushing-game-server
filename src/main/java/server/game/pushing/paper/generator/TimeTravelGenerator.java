package server.game.pushing.paper.generator;

import server.game.pushing.paper.store.bank.Bank;

import java.util.Random;

import static server.game.pushing.paper.TransactionType.TimeTravel;

public class TimeTravelGenerator extends Generator {
    public TimeTravelGenerator(Random random, Bank bank) {
        super(random, bank);
        transactionType = TimeTravel;
    }

    public String generateTransaction() {
        String transaction = "";
        while (!validators.handleTransaction(transaction)) {
            int months = random.nextInt(bank.getMaxTimeTravel());

            transaction = String.format("%s %s", transactionType, months).toLowerCase();
        }

        return transaction;
    }
}
