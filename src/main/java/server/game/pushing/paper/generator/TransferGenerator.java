package server.game.pushing.paper.generator;

import server.game.pushing.paper.store.bank.Bank;

import java.util.Random;

import static java.lang.Math.min;
import static server.game.pushing.paper.TransactionType.Transfer;

public class TransferGenerator extends Generator {
    public TransferGenerator(Random random, Bank bank) {
        super(random, bank);
        transactionType = Transfer;
    }

    public String generateTransaction() {
        checkException();

        String transaction = "";
        while (!validators.handleTransaction(transaction)) {
            String payingID = generateID(false);
            String receivingID = generateID(false);
            double transferAmount = generateAmount(min(bank.getAccount(payingID).getMaxWithdrawAmount(), bank.getAccount(receivingID).getMaxDepositAmount()));

            transaction = String.format("%s %s %s %.2f", transactionType, payingID, receivingID, transferAmount).toLowerCase();
        }

        return transaction;
    }
}
