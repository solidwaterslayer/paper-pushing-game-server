package server.game.pushing.paper.ledgervalidator.transactionchain.transactionvalidator;

import server.game.pushing.paper.ledgervalidator.transactionchain.TransactionChain;
import server.game.pushing.paper.ledgervalidator.bank.Bank;
import server.game.pushing.paper.ledgervalidator.transactionchain.TransactionType;

import static java.lang.Integer.parseInt;

public class PassTimeValidator extends TransactionChain {
    public PassTimeValidator(Bank bank) {
        super(bank);
        transactionType = TransactionType.PassTime;
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
