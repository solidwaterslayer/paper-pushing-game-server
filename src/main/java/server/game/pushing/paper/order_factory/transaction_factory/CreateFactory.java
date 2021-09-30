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

    public String getTransaction() {
        AccountType accountType = AccountType.values()[random.nextInt(AccountType.values().length)];
        String transaction = "";

        while (!validator.handle(transaction)) {
            String id = random.ints(48, 58).limit(8).collect(
                    StringBuilder :: new,
                    StringBuilder :: appendCodePoint,
                    StringBuilder :: append
            ).toString();
            double apr = bank.getMaxAPR() * random.nextDouble();
            double initialCDBalance = bank.getMaxInitialCDBalance() * random.nextDouble();
            transaction = String.format("%s %s %s %.2f %.2f", transactionType, accountType, id, apr, initialCDBalance).toLowerCase();
        }

        return transaction;
    }
}
