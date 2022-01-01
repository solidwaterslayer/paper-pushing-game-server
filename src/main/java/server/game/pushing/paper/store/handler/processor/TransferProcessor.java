package server.game.pushing.paper.store.handler.processor;

import server.game.pushing.paper.store.bank.Bank;
import server.game.pushing.paper.store.handler.Handler;

import static java.lang.Double.parseDouble;
import static server.game.pushing.paper.TransactionType.Transfer;

public class TransferProcessor extends Handler {
    public TransferProcessor(Bank bank) {
        super(bank);
        transactionType = Transfer;
    }

    @Override
    public boolean handleTransaction(String[] transactionArguments) {
        if (transactionArguments[0].equalsIgnoreCase(transactionType.name())) {
            bank.transfer(transactionArguments[1], transactionArguments[2], parseDouble(transactionArguments[3]));
            return true;
        }

        return next != null && next.handleTransaction(transactionArguments);
    }
}
