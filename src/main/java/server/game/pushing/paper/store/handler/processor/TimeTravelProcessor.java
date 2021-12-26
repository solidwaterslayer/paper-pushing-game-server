package server.game.pushing.paper.store.handler.processor;

import server.game.pushing.paper.store.bank.Bank;
import server.game.pushing.paper.store.handler.Handler;

import static java.lang.Integer.parseInt;
import static server.game.pushing.paper.store.handler.TransactionType.TimeTravel;

public class TimeTravelProcessor extends Handler {
    public TimeTravelProcessor(Bank bank) {
        super(bank);
        transactionType = TimeTravel;
    }

    @Override
    public boolean handleTransaction(String[] transactionArguments) {
        if ((transactionArguments[0] + transactionArguments[1]).equalsIgnoreCase(transactionType.name())) {
            bank.timeTravel(parseInt(transactionArguments[2]));
            return true;
        }

        return next != null && next.handleTransaction(transactionArguments);
    }
}
