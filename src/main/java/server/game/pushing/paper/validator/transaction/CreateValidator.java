package server.game.pushing.paper.validator.transaction;

import server.game.pushing.paper.bank.Bank;

public class CreateValidator extends TransactionValidator {
    public CreateValidator(TransactionValidator nextHandler, Bank bank) {
        super(nextHandler, bank);
    }

    protected boolean isTransactionValid(String[] transactionArguments) {
        try {
            if (transactionArguments[0].equalsIgnoreCase("create")
                    && (isCreateCheckingTransactionValid(transactionArguments)
                    || isCreateSavingsTransactionValid(transactionArguments)
                    || isCreateCDTransactionValid(transactionArguments))) {
                return true;
            } else {
                return nextHandler != null && nextHandler.isTransactionValid(transactionArguments);
            }
        } catch (ArrayIndexOutOfBoundsException | NumberFormatException exception) {
            return false;
        }
    }

    protected boolean isCreateCheckingTransactionValid(String[] transactionArguments) {
        return transactionArguments[1].equalsIgnoreCase("checking")
                && bank.isIDValid(transactionArguments[2])
                && bank.isAPRValid(Double.parseDouble(transactionArguments[3]));
    }

    protected boolean isCreateSavingsTransactionValid(String[] transactionArguments) {
        return transactionArguments[1].equalsIgnoreCase("savings")
                && bank.isIDValid(transactionArguments[2])
                && bank.isAPRValid(Double.parseDouble(transactionArguments[3]));
    }

    protected boolean isCreateCDTransactionValid(String[] transactionArguments) {
        return transactionArguments[1].equalsIgnoreCase("cd")
                && bank.isIDValid(transactionArguments[2])
                && bank.isAPRValid(Double.parseDouble(transactionArguments[3]))
                && bank.isInitialCDBalanceValid(Double.parseDouble(transactionArguments[4]));
    }
}