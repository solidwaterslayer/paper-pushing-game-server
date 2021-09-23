package server.game.pushing.paper.order_factory.transaction_factory;

import server.game.pushing.paper.store.bank.Bank;
import server.game.pushing.paper.store.chain_of_responsibility.TransactionType;

import java.util.Random;

public class DepositFactory extends TransactionFactory {
    public DepositFactory(Bank bank, Random random) {
        super(bank, random);

        transactionType = TransactionType.Deposit;
    }

    public String getTransaction() {
        String id = random.ints(48, 58).limit(8).collect(
                StringBuilder :: new,
                StringBuilder :: appendCodePoint,
                StringBuilder :: append
        ).toString();
        double depositAmount = (bank.getMaxInitialCDBalance() - bank.getMinInitialCDBalance()) * random.nextDouble() + bank.getMinInitialCDBalance();

        return String.format("%s %s %.2f", transactionType, id, depositAmount);
    }
}
