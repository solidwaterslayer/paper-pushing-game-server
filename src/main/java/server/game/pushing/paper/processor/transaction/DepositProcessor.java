package server.game.pushing.paper.processor.transaction;

import server.game.pushing.paper.TransactionChain;
import server.game.pushing.paper.bank.Bank;

public class DepositProcessor extends TransactionChain {
    public DepositProcessor(Bank bank) {
        super(bank);
    }

    @Override
    public boolean handle(String[] transactionArguments) {
        if (transactionArguments[0].equalsIgnoreCase("deposit")) {
            bank.deposit(transactionArguments[1], Double.parseDouble(transactionArguments[2]));
            return true;
        } else {
            return next != null && next.handle(transactionArguments);
        }
    }
}
