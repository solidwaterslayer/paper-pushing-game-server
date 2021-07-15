package server.game.pushing.paper.validator.transaction;

import server.game.pushing.paper.bank.Bank;

public class PassTimeValidator extends TransactionValidator {
    public PassTimeValidator(TransactionValidator nextHandler, Bank bank) {
        super(nextHandler, bank);
    }

    public boolean isTransactionValid(String[] transactionArguments) {
        try {
            if (transactionArguments[0].equalsIgnoreCase("pass")
                    && transactionArguments[1].equalsIgnoreCase("time")
                    && bank.isPassTimeValid(Integer.parseInt(transactionArguments[2]))) {
                return true;
            } else {
                return nextHandler != null && nextHandler.isTransactionValid(transactionArguments);
            }
        } catch (ArrayIndexOutOfBoundsException | NumberFormatException exception) {
            return false;
        }
    }
}