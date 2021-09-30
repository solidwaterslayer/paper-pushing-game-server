package server.game.pushing.paper.order_factory.transaction_factory;

import server.game.pushing.paper.store.bank.Bank;
import server.game.pushing.paper.store.bank.account.AccountType;
import server.game.pushing.paper.store.chain_of_responsibility.TransactionType;

import java.util.Random;

public class WithdrawFactory extends TransactionFactory {
    public WithdrawFactory(Bank bank, Random random) {
        super(bank, random);

        transactionType = TransactionType.Withdraw;
    }

    public String getTransaction() {
        checkException();

        String transaction = "";
        while (!validator.handle(transaction)) {
            String id = bank.getIDs().get(random.nextInt(bank.getIDs().size()));
            double withdrawAmount = bank.getAccount(id).getMaxDepositAmount() * random.nextDouble();
            transaction = String.format("%s %s %.2f", transactionType, id, withdrawAmount).toLowerCase();
        }

        return transaction;
    }

    private void checkException() {
        if (bank.isEmpty()) {
            throw new IllegalArgumentException("[error] bank is empty");
        }
        if (!bank.containsChecking()) {
            throw new IllegalArgumentException("[error] bank contains 0 checking");
        }
    }
}
