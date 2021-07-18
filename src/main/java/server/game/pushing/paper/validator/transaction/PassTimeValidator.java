package server.game.pushing.paper.validator.transaction;

import server.game.pushing.paper.TransactionChain;
import server.game.pushing.paper.bank.Bank;

import static java.lang.Integer.parseInt;

public class PassTimeValidator extends TransactionChain {
    public PassTimeValidator(Bank bank) {
        super(bank);
    }

    @Override
    public boolean handle(String[] transactionArguments) {
        try {
            if (transactionArguments[0].equalsIgnoreCase("pass")
                    && transactionArguments[1].equalsIgnoreCase("time")
                    && bank.isPassTimeValid(parseInt(transactionArguments[2]))) {
                return true;
            } else {
                return next != null && next.handle(transactionArguments);
            }
        } catch (ArrayIndexOutOfBoundsException | NumberFormatException exception) {
            return false;
        }
    }
}
