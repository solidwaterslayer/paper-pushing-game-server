package server.game.pushing.paper.order_generator.transaction_generator;

import server.game.pushing.paper.store.bank.Bank;
import server.game.pushing.paper.store.chain_of_responsibility.TransactionType;

import java.util.Random;

public class TimeTravelGenerator extends TransactionGenerator {
    public TimeTravelGenerator(Bank bank, Random random) {
        super(bank, random);

        transactionType = TransactionType.TimeTravel;
    }

    public String getTransaction() {
        String transaction = "";

        while (!validator.handle(transaction)) {
            int months = random.nextInt(bank.getMaxMonths());
            transaction = String.format("%s %s", transactionType, months).toLowerCase();
        }

        return transaction;
    }
}