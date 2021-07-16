package server.game.pushing.paper.processor.transaction;

import server.game.pushing.paper.TransactionHandler;
import server.game.pushing.paper.bank.Bank;

public class TransferProcessor extends TransactionHandler {
    public TransferProcessor(TransactionHandler nextHandler, Bank bank) {
        super(nextHandler, bank);
    }

    @Override
    public boolean handle(String[] transactionArguments) {
        if (transactionArguments[0].equalsIgnoreCase("transfer")) {
            bank.transfer(transactionArguments[1], transactionArguments[2], Double.parseDouble(transactionArguments[3]));
            return true;
        } else {
            return nextHandler != null && nextHandler.handle(transactionArguments);
        }
    }
}
