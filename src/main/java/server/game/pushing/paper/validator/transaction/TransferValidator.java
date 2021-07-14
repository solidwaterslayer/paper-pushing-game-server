package server.game.pushing.paper.validator.transaction;

import server.game.pushing.paper.bank.Bank;

public class TransferValidator extends TransactionValidator {
    public TransferValidator(TransactionValidator nextHandler, Bank bank) {
        super(nextHandler, bank);
    }

    public boolean isTransactionValid(String[] transactionArguments) {
        try {
            if (transactionArguments[0].equalsIgnoreCase("transfer") && bank.isTransferValid(transactionArguments[1], transactionArguments[2], Double.parseDouble(transactionArguments[3]))) {
                return true;
            } else {
                return nextHandler.isTransactionValid(transactionArguments);
            }
        } catch (IndexOutOfBoundsException | IllegalArgumentException | NullPointerException exception) {
            return false;
        }
    }
}
