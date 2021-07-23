package server.game.pushing.paper.ledgervalidator.transactionchain.transactionvalidator;

import server.game.pushing.paper.ledgervalidator.transactionchain.TransactionChain;
import server.game.pushing.paper.ledgervalidator.bank.Bank;
import server.game.pushing.paper.ledgervalidator.transactionchain.TransactionType;

import static java.lang.Double.parseDouble;

public class CreateValidator extends TransactionChain {
    public CreateValidator(Bank bank) {
        super(bank);
        transactionType = TransactionType.Create;
    }

    @Override
    public boolean handle(String[] transactionArguments) {
        try {
            if (transactionArguments[0].equalsIgnoreCase("create")
                    && (isCreateCheckingTransactionValid(transactionArguments)
                    || isCreateSavingsTransactionValid(transactionArguments)
                    || isCreateCDTransactionValid(transactionArguments))) {
                return true;
            } else {
                return next != null && next.handle(transactionArguments);
            }
        } catch (ArrayIndexOutOfBoundsException | NumberFormatException exception) {
            return false;
        }
    }

    protected boolean isCreateCheckingTransactionValid(String[] transactionArguments) {
        return transactionArguments[1].equalsIgnoreCase("checking")
                && bank.isIDValid(transactionArguments[2])
                && bank.isAPRValid(parseDouble(transactionArguments[3]));
    }

    protected boolean isCreateSavingsTransactionValid(String[] transactionArguments) {
        return transactionArguments[1].equalsIgnoreCase("savings")
                && bank.isIDValid(transactionArguments[2])
                && bank.isAPRValid(parseDouble(transactionArguments[3]));
    }

    protected boolean isCreateCDTransactionValid(String[] transactionArguments) {
        return transactionArguments[1].equalsIgnoreCase("cd")
                && bank.isIDValid(transactionArguments[2])
                && bank.isAPRValid(parseDouble(transactionArguments[3]))
                && bank.isInitialCDBalanceValid(parseDouble(transactionArguments[4]));
    }
}
