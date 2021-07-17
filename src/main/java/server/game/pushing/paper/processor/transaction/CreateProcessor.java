package server.game.pushing.paper.processor.transaction;

import server.game.pushing.paper.TransactionHandler;
import server.game.pushing.paper.bank.Bank;

public class CreateProcessor extends TransactionHandler {
    public CreateProcessor(Bank bank) {
        super(bank);
    }

    @Override
    public boolean handle(String[] transactionArguments) {
        if (transactionArguments[0].equalsIgnoreCase("create")) {
            String id = transactionArguments[2];
            double apr = Double.parseDouble(transactionArguments[3]);

            switch (transactionArguments[1].toLowerCase()) {
                case "checking":
                    bank.createChecking(id, apr);
                    return true;
                case "savings":
                    bank.createSavings(id, apr);
                    return true;
                case "cd":
                    bank.createCD(id, apr, Double.parseDouble(transactionArguments[4]));
                    return true;
                default:
                    return false;
            }
        } else {
            return nextHandler != null && nextHandler.handle(transactionArguments);
        }
    }
}
