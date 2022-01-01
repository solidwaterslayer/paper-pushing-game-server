package server.game.pushing.paper.generator;

import server.game.pushing.paper.store.bank.Bank;

import java.util.Random;

import static server.game.pushing.paper.TransactionType.Withdraw;

public class WithdrawGenerator extends Generator {
    public WithdrawGenerator(Random random, Bank bank) {
        super(random, bank);
        transactionType = Withdraw;
    }

    public String generateTransaction() {
        checkException();

        String transaction = "";
        while (!validators.handleTransaction(transaction)) {
            String id = generateID(false);
            double withdrawAmount = generateNumber(bank.getAccount(id).getMaxDepositAmount());

            transaction = String.format("%s %s %.2f", transactionType, id, withdrawAmount).toLowerCase();
        }

        return transaction;
    }
}
