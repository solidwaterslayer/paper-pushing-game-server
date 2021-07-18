package server.game.pushing.paper.validator.transaction;

import server.game.pushing.paper.TransactionChain;
import server.game.pushing.paper.bank.Bank;

import static java.lang.Double.parseDouble;

public class TransferValidator extends TransactionChain {
    public TransferValidator(Bank bank) {
        super(bank);
    }

    @Override
    public boolean handle(String[] transactionArguments) {
        try {
            if (transactionArguments[0].equalsIgnoreCase("transfer") && bank.isTransferValid(transactionArguments[1], transactionArguments[2], parseDouble(transactionArguments[3]))) {
                return true;
            } else {
                return next != null && next.handle(transactionArguments);
            }
        } catch (ArrayIndexOutOfBoundsException | NumberFormatException exception) {
            return false;
        }
    }
}
