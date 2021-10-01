package server.game.pushing.paper.order_factory.transaction_factory;

import server.game.pushing.paper.store.bank.Bank;
import server.game.pushing.paper.store.chain_of_responsibility.TransactionType;

import java.util.Random;

import static java.lang.Math.min;

public class TransferFactory extends TransactionFactory {
    public TransferFactory(Bank bank, Random random) {
        super(bank, random);

        transactionType = TransactionType.Transfer;
    }

    public String getTransaction() {
        checkException();

        String transaction = "";
        while (!validator.handle(transaction)) {
            String fromID = bank.getIDs().get(random.nextInt(bank.getIDs().size()));
            String toID = bank.getIDs().get(random.nextInt(bank.getIDs().size()));
            double withdrawAmount = min(bank.getAccount(fromID).getMaxWithdrawAmount(), bank.getAccount(toID).getMaxDepositAmount()) * random.nextDouble();
            transaction = String.format("%s %s %s %.2f", transactionType, fromID, toID, withdrawAmount).toLowerCase();
        }

        return transaction;
    }
}
