package server.game.pushing.paper.store.handler.validator;

import server.game.pushing.paper.store.bank.Bank;
import server.game.pushing.paper.store.handler.Handler;
import server.game.pushing.paper.store.handler.TransactionType;

import static java.lang.Integer.parseInt;

public class TimeTravelValidator extends Handler {
    public TimeTravelValidator(Bank bank) {
        super(bank);
        transactionType = TransactionType.TimeTravel;
    }

    @Override
    public boolean handleTransaction(String[] transactionArguments) {
        try {
            if ((transactionArguments[0] + transactionArguments[1]).equalsIgnoreCase(transactionType.name()) && bank.isTimeTravelValid(
                    parseInt(transactionArguments[2])
            )) {
                return true;
            } else {
                return next != null && next.handleTransaction(transactionArguments);
            }
        } catch (ArrayIndexOutOfBoundsException | NumberFormatException exception) {
            return false;
        }
    }
}
