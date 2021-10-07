package server.game.pushing.paper.order_factory.transaction_factory;

import server.game.pushing.paper.store.bank.Bank;
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
            String id = getRandomID();
            double withdrawAmount = getRandomAmount(bank.getAccount(id).getMaxDepositAmount());
            transaction = String.format("%s %s %.2f", transactionType, id, withdrawAmount).toLowerCase();
        }

        return transaction;
    }
}
