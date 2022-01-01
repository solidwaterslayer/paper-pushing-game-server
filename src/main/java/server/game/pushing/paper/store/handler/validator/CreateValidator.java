package server.game.pushing.paper.store.handler.validator;

import server.game.pushing.paper.store.bank.Bank;
import server.game.pushing.paper.store.handler.Handler;

import static java.lang.Double.parseDouble;
import static server.game.pushing.paper.store.bank.AccountType.*;
import static server.game.pushing.paper.store.handler.TransactionType.Create;

public class CreateValidator extends Handler {
    public CreateValidator(Bank bank) {
        super(bank);
        transactionType = Create;
    }

    @Override
    public boolean handleTransaction(String[] transactionArguments) {
        try {
            if (handleCheckingTransaction(transactionArguments)
                    || handleSavingsTransaction(transactionArguments)
                    || handleCDTransaction(transactionArguments)
            ) {
                return true;
            } else {
                return next != null && next.handleTransaction(transactionArguments);
            }
        } catch (ArrayIndexOutOfBoundsException | NumberFormatException exception) {
            return false;
        }
    }

    private boolean handleCheckingTransaction(String[] transactionArguments) {
        return transactionArguments[0].equalsIgnoreCase(transactionType.name())
                && transactionArguments[1].equalsIgnoreCase(Checking.name())
                && bank.isIDValid(transactionArguments[2]);
    }

    private boolean handleSavingsTransaction(String[] transactionArguments) {
        return transactionArguments[0].equalsIgnoreCase(transactionType.name())
                && transactionArguments[1].equalsIgnoreCase(Savings.name())
                && bank.isIDValid(transactionArguments[2]);
    }

    private boolean handleCDTransaction(String[] transactionArguments) {
        return transactionArguments[0].equalsIgnoreCase(transactionType.name())
                && transactionArguments[1].equalsIgnoreCase(CD.name())
                && bank.isIDValid(transactionArguments[2])
                && bank.isCDBalanceValid(parseDouble(transactionArguments[3]));
    }
}
