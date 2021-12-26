package server.game.pushing.paper.order_generator.transaction_generator;

import server.game.pushing.paper.store.bank.Bank;
import server.game.pushing.paper.store.handler.TransactionType;

import java.util.Random;

public class WithdrawGenerator extends TransactionGenerator {
    public WithdrawGenerator(Bank bank, Random random) {
        super(bank, random);

        transactionType = TransactionType.Withdraw;
    }

    public String getTransaction() {
        checkException();

        String transaction = "";
        while (!validator.handleTransaction(transaction)) {
            String id = getRandomID();
            double withdrawAmount = getRandomAmount(bank.getAccount(id).getMaxDepositAmount());
            transaction = String.format("%s %s %.2f", transactionType, id, withdrawAmount).toLowerCase();
        }

        return transaction;
    }
}
