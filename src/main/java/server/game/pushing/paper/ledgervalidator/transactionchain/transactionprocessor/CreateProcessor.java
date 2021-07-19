package server.game.pushing.paper.ledgervalidator.transactionchain.transactionprocessor;

import server.game.pushing.paper.ledgervalidator.bank.Bank;
import server.game.pushing.paper.ledgervalidator.transactionchain.TransactionChain;

import static java.lang.Double.parseDouble;

public class CreateProcessor extends TransactionChain {
    public CreateProcessor(Bank bank) {
        super(bank);
    }

    @Override
    public boolean handle(String[] transactionArguments) {
        if (transactionArguments[0].equalsIgnoreCase("create")
                && (transactionArguments[1].equalsIgnoreCase("checking")
                || transactionArguments[1].equalsIgnoreCase("savings")
                || transactionArguments[1].equalsIgnoreCase("cd"))) {
            String id = transactionArguments[2];
            double apr = parseDouble(transactionArguments[3]);

            switch (transactionArguments[1].toLowerCase()) {
                case "checking":
                    bank.createChecking(id, apr);
                    break;
                case "savings":
                    bank.createSavings(id, apr);
                    break;
                default:
                    bank.createCD(id, apr, parseDouble(transactionArguments[4]));
            }

            return true;
        } else {
            return next != null && next.handle(transactionArguments);
        }
    }
}
