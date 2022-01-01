package server.game.pushing.paper.store.handler.validator;

import server.game.pushing.paper.store.bank.Bank;
import server.game.pushing.paper.store.handler.Handler;

import static java.lang.Double.parseDouble;
import static server.game.pushing.paper.TransactionType.Deposit;

public class DepositValidator extends Handler {
    public DepositValidator(Bank bank) {
        super(bank);
        transactionType = Deposit;
    }

    @Override
    public boolean handleTransaction(String[] transactionArguments) {
        try {
            if (transactionArguments[0].equalsIgnoreCase(transactionType.name()) && bank.isDepositAmountValid(
                    transactionArguments[1],
                    parseDouble(transactionArguments[2])
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
