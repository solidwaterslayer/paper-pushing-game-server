package server.game.pushing.paper.order_factory.transaction_factory;

import server.game.pushing.paper.store.bank.Bank;
import server.game.pushing.paper.store.chain_of_responsibility.TransactionType;

import java.util.Random;

public class PassTimeFactory extends TransactionFactory {
    public PassTimeFactory(Bank bank, Random random) {
        super(bank, random);

        transactionType = TransactionType.PassTime;
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
