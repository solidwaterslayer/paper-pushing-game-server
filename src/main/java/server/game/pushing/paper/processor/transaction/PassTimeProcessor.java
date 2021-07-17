package server.game.pushing.paper.processor.transaction;

import server.game.pushing.paper.TransactionHandler;
import server.game.pushing.paper.bank.Bank;

public class PassTimeProcessor extends TransactionHandler {
    public PassTimeProcessor(Bank bank) {
        super(bank);
    }

    @Override
    public boolean handle(String[] transactionArguments) {
        if (transactionArguments[0].equalsIgnoreCase("pass")
                && transactionArguments[1].equalsIgnoreCase("time")) {
            bank.passTime(Integer.parseInt(transactionArguments[2]));
            return true;
        } else {
            return nextHandler != null && nextHandler.handle(transactionArguments);
        }
    }
}
