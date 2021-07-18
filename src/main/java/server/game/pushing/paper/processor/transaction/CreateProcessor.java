package server.game.pushing.paper.processor.transaction;

import server.game.pushing.paper.TransactionHandler;
import server.game.pushing.paper.bank.Bank;

public class CreateProcessor extends TransactionHandler {
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
            double apr = Double.parseDouble(transactionArguments[3]);

            switch (transactionArguments[1].toLowerCase()) {
                case "checking":
                    bank.createChecking(id, apr);
                    break;
                case "savings":
                    bank.createSavings(id, apr);
                    break;
                default:
                    bank.createCD(id, apr, Double.parseDouble(transactionArguments[4]));
            }

            return true;
        } else {
            return nextHandler != null && nextHandler.handle(transactionArguments);
        }
    }
}
