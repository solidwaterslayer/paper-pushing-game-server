package server.game.pushing.paper.order_factory.transaction_factory;

import server.game.pushing.paper.store.bank.Bank;
import server.game.pushing.paper.store.bank.account.AccountType;
import server.game.pushing.paper.store.chain_of_responsibility.TransactionType;

import java.util.Random;

public class DepositFactory extends TransactionFactory {
    public DepositFactory(Bank bank, Random random) {
        super(bank, random);

        transactionType = TransactionType.Deposit;
    }

    public String getTransaction() {
        checkException();

        String transaction = "";
        while (!validator.handle(transaction)) {
            String id = bank.getIDs().get(random.nextInt(bank.getIDs().size()));
            double depositAmount = bank.getAccount(id).getMaxDepositAmount() * random.nextDouble();
            transaction = String.format("%s %s %.2f", transactionType, id, depositAmount).toLowerCase();
        }

        return transaction;
    }

    private void checkException() {
        if (bank.isEmpty()) {
            throw new IllegalArgumentException("[error] bank is empty");
        }
        if (!bank.containsChecking() || !bank.containsSavings()) {
            throw new IllegalArgumentException("[error] bank contains 0 checking or savings");
        }
    }
}
