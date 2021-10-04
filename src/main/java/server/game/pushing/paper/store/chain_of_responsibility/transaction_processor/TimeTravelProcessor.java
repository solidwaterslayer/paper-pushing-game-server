package server.game.pushing.paper.store.chain_of_responsibility.transaction_processor;

import server.game.pushing.paper.store.bank.Bank;
import server.game.pushing.paper.store.chain_of_responsibility.ChainOfResponsibility;
import server.game.pushing.paper.store.chain_of_responsibility.TransactionType;

import static java.lang.Integer.parseInt;

public class TimeTravelProcessor extends ChainOfResponsibility {
    public TimeTravelProcessor(Bank bank) {
        super(bank);
        transactionType = TransactionType.TimeTravel;
    }

    @Override
    public boolean handle(String[] transactionArguments) {
        if ((transactionArguments[0] + transactionArguments[1]).equalsIgnoreCase(transactionType.name())) {
            bank.timeTravel(parseInt(transactionArguments[2]));
            return true;
        }

        return next != null && next.handle(transactionArguments);
    }
}
