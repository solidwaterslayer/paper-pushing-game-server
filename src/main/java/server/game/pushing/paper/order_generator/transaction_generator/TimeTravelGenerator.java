package server.game.pushing.paper.order_generator.transaction_generator;

import server.game.pushing.paper.store.bank.Bank;
import server.game.pushing.paper.store.handler.TransactionType;

import java.util.Random;

public class TimeTravelGenerator extends TransactionGenerator {
    public TimeTravelGenerator(Bank bank, Random random) {
        super(bank, random);

        transactionType = TransactionType.TimeTravel;
    }

    public String getTransaction() {
        String transaction = "";

        while (!validator.handleTransaction(transaction)) {
            int months = random.nextInt(bank.getMaxTimeTravel());
            transaction = String.format("%s %s", transactionType, months).toLowerCase();
        }

        return transaction;
    }
}
