package server.game.pushing.paper.validator.transaction;

import server.game.pushing.paper.TransactionHandler;
import server.game.pushing.paper.bank.Bank;

public class TransferValidator extends TransactionHandler {
    public TransferValidator(TransactionHandler nextHandler, Bank bank) {
        super(nextHandler, bank);
    }

    @Override
    public boolean handle(String[] transactionArguments) {
        try {
            if (transactionArguments[0].equalsIgnoreCase("transfer") && bank.isTransferValid(transactionArguments[1], transactionArguments[2], Double.parseDouble(transactionArguments[3]))) {
                return true;
            } else {
                return nextHandler != null && nextHandler.handle(transactionArguments);
            }
        } catch (ArrayIndexOutOfBoundsException | NumberFormatException exception) {
            return false;
        }
    }
}
