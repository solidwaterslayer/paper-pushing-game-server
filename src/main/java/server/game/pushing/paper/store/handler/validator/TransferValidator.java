package server.game.pushing.paper.store.handler.validator;

import server.game.pushing.paper.store.bank.Bank;
import server.game.pushing.paper.store.handler.Handler;

import static java.lang.Double.parseDouble;
import static server.game.pushing.paper.TransactionType.Transfer;

public class TransferValidator extends Handler {
    public TransferValidator(Bank bank) {
        super(bank);
        transactionType = Transfer;
    }

    @Override
    public boolean handleTransaction(String[] transactionArguments) {
        try {
            if (transactionArguments[0].equalsIgnoreCase(transactionType.name()) && bank.isTransferAmountValid(
                    transactionArguments[1],
                    transactionArguments[2],
                    parseDouble(transactionArguments[3])
            )) {
                return true;
            } else {
                return next != null && next.handleTransaction(transactionArguments);
            }
        } catch (ArrayIndexOutOfBoundsException | NumberFormatException exception) {
            return false;
        }
    }
}
