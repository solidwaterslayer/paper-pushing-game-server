package server.game.pushing.paper.processor.transaction;

import server.game.pushing.paper.TransactionHandler;
import server.game.pushing.paper.bank.Bank;

public class DepositProcessor extends TransactionHandler {
    public DepositProcessor(Bank bank) {
        super(bank);
    }

    @Override
    public boolean handle(String[] transactionArguments) {
        if (transactionArguments[0].equalsIgnoreCase("deposit")) {
            bank.deposit(transactionArguments[1], Double.parseDouble(transactionArguments[2]));
            return true;
        } else {
            return nextHandler != null && nextHandler.handle(transactionArguments);
        }
    }
}
