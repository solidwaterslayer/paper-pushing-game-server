package server.game.pushing.paper.validator.transaction;

import server.game.pushing.paper.bank.Bank;

public class DepositValidator extends TransactionValidator {
    public DepositValidator(TransactionValidator nextHandler, Bank bank) {
        super(nextHandler, bank);
    }

    public boolean isTransactionValid(String[] transactionArguments) {
        try {
            if (transactionArguments[0].equalsIgnoreCase("deposit") && bank.isDepositValid(transactionArguments[1], Double.parseDouble(transactionArguments[2]))) {
                return true;
            } else {
                return nextHandler.isTransactionValid(transactionArguments);
            }
        } catch (IndexOutOfBoundsException | IllegalArgumentException | NullPointerException exception) {
            return false;
        }
    }
}
