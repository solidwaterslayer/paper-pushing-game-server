package server.game.pushing.paper.ledgervalidator.transactionchain.transactionprocessor;

import server.game.pushing.paper.ledgervalidator.bank.Bank;
import server.game.pushing.paper.ledgervalidator.transactionchain.TransactionChain;

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
