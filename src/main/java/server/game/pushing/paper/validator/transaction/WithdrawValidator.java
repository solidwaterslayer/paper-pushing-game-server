package server.game.pushing.paper.validator.transaction;

import server.game.pushing.paper.TransactionHandler;
import server.game.pushing.paper.bank.Bank;

public class WithdrawValidator extends TransactionHandler {
    public WithdrawValidator(TransactionHandler nextHandler, Bank bank) {
        super(nextHandler, bank);
    }

    @Override
    public boolean handle(String[] transactionArguments) {
        try {
            if (transactionArguments[0].equalsIgnoreCase("withdraw") && bank.isWithdrawValid(transactionArguments[1], Double.parseDouble(transactionArguments[2]))) {
                return true;
            } else {
                return nextHandler != null && nextHandler.handle(transactionArguments);
            }
        } catch (ArrayIndexOutOfBoundsException | NumberFormatException exception) {
            return false;
        }
    }
}
