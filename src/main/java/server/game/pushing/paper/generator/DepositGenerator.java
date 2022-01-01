package server.game.pushing.paper.generator;

import server.game.pushing.paper.store.bank.Bank;

import java.util.Random;

import static server.game.pushing.paper.TransactionType.Deposit;

public class DepositGenerator extends Generator {
    public DepositGenerator(Random random, Bank bank) {
        super(random, bank);
        transactionType = Deposit;
    }

    public String generateTransaction() {
        checkException();

        String transaction = "";
        while (!validators.handleTransaction(transaction)) {
            String id = generateID(false);
            double depositAmount = generateNumber(bank.getAccount(id).getMaxDepositAmount());

            transaction = String.format("%s %s %.2f", transactionType, id, depositAmount).toLowerCase();
        }

        return transaction;
    }
}
