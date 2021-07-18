package server.game.pushing.paper.processor.transaction;

import server.game.pushing.paper.TransactionChain;
import server.game.pushing.paper.bank.Bank;

import static java.lang.Integer.parseInt;

public class PassTimeProcessor extends TransactionChain {
    public PassTimeProcessor(Bank bank) {
        super(bank);
    }

    @Override
    public boolean handle(String[] transactionArguments) {
        if (transactionArguments[0].equalsIgnoreCase("pass")
                && transactionArguments[1].equalsIgnoreCase("time")) {
            bank.passTime(parseInt(transactionArguments[2]));
            return true;
        } else {
            return next != null && next.handle(transactionArguments);
        }
    }
}
