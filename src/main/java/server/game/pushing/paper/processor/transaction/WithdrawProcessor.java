package server.game.pushing.paper.processor.transaction;

import server.game.pushing.paper.TransactionHandler;
import server.game.pushing.paper.bank.Bank;

public class WithdrawProcessor extends TransactionHandler {
    public WithdrawProcessor(TransactionHandler nextHandler, Bank bank) {
        super(nextHandler, bank);
    }

    @Override
    public boolean handle(String[] transactionArguments) {
        if (transactionArguments[0].equalsIgnoreCase("withdraw")) {
            bank.withdraw(transactionArguments[1], Double.parseDouble(transactionArguments[2]));
            return true;
        } else {
            return nextHandler != null && nextHandler.handle(transactionArguments);
        }
    }
}
