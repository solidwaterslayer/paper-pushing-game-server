package server.game.pushing.paper.processor.transaction;

import server.game.pushing.paper.TransactionChain;
import server.game.pushing.paper.bank.Bank;

import static java.lang.Double.parseDouble;

public class TransferProcessor extends TransactionChain {
    public TransferProcessor(Bank bank) {
        super(bank);
    }

    @Override
    public boolean handle(String[] transactionArguments) {
        if (transactionArguments[0].equalsIgnoreCase("transfer")) {
            bank.transfer(transactionArguments[1], transactionArguments[2], parseDouble(transactionArguments[3]));
            return true;
        } else {
            return next != null && next.handle(transactionArguments);
        }
    }
}
