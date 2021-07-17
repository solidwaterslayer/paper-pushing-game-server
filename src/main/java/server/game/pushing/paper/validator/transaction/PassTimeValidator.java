package server.game.pushing.paper.validator.transaction;

import server.game.pushing.paper.TransactionHandler;
import server.game.pushing.paper.bank.Bank;

public class PassTimeValidator extends TransactionHandler {
    public PassTimeValidator(Bank bank) {
        super(bank);
    }

    @Override
    public boolean handle(String[] transactionArguments) {
        try {
            if (transactionArguments[0].equalsIgnoreCase("pass")
                    && transactionArguments[1].equalsIgnoreCase("time")
                    && bank.isPassTimeValid(Integer.parseInt(transactionArguments[2]))) {
                return true;
            } else {
                return nextHandler != null && nextHandler.handle(transactionArguments);
            }
        } catch (ArrayIndexOutOfBoundsException | NumberFormatException exception) {
            return false;
        }
    }
}
