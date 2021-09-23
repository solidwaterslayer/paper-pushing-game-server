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

    public String getTransaction() throws Exception {
        checkException();

        String id = bank.getIDs().get(random.nextInt(bank.getIDs().size()));
        double depositAmount = bank.getAccount(id).getMaxDepositAmount() * random.nextDouble();
        while (bank.getAccount(id).getAccountType() == AccountType.CD && depositAmount == 0) {
            id = bank.getIDs().get(random.nextInt(bank.getIDs().size()));
            depositAmount = bank.getAccount(id).getMaxDepositAmount() * random.nextDouble();
        }

        return String.format("%s %s %.2f", transactionType, id, depositAmount).toLowerCase();
    }

    private void checkException() throws Exception {
        if (bank.isEmpty()) {
            throw new Exception("[error] bank is empty");
        }
        if (bank.containsOnlyCD()) {
            throw new Exception("[error] bank contains only cd");
        }
    }
}
