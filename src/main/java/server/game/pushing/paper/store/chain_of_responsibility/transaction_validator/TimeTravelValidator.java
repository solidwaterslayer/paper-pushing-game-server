package server.game.pushing.paper.store.chain_of_responsibility.transaction_validator;

import server.game.pushing.paper.store.bank.Bank;
import server.game.pushing.paper.store.chain_of_responsibility.ChainOfResponsibility;
import server.game.pushing.paper.store.chain_of_responsibility.TransactionType;

import static java.lang.Integer.parseInt;

public class TimeTravelValidator extends ChainOfResponsibility {
    public TimeTravelValidator(Bank bank) {
        super(bank);
        transactionType = TransactionType.TimeTravel;
    }

    @Override
    public boolean handle(String[] transactionArguments) {
        try {
            if ((transactionArguments[0] + transactionArguments[1]).equalsIgnoreCase(transactionType.name())
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
