package server.game.pushing.paper.validator.transaction;

import server.game.pushing.paper.bank.Bank;

public class CreateValidator extends TransactionValidator {
    public CreateValidator(TransactionValidator nextHandler, Bank bank) {
        super(nextHandler, bank);
    }

    public boolean isTransactionValid(String[] transactionArguments) {
        try {
            if (transactionArguments[0].equalsIgnoreCase("create")
                    && bank.isAccountTypeValid(transactionArguments[1])
                    && (isCreateCheckingValid(transactionArguments) || isCreateSavingsValid(transactionArguments) || isCreateCDValid(transactionArguments))) {
                return true;
            } else {
                return nextHandler.isTransactionValid(transactionArguments);
            }
        } catch (IndexOutOfBoundsException | IllegalArgumentException exception) {
            return false;
        }
    }

    protected boolean isCreateCheckingValid(String[] transactionArguments) {
        return transactionArguments[1].equalsIgnoreCase("checking")
                && bank.isIDValid(transactionArguments[2])
                && bank.isAPRValid(Double.parseDouble(transactionArguments[3]));
    }

    protected boolean isCreateSavingsValid(String[] transactionArguments) {
        return transactionArguments[1].equalsIgnoreCase("savings")
                && bank.isIDValid(transactionArguments[2])
                && bank.isAPRValid(Double.parseDouble(transactionArguments[3]));
    }

    protected boolean isCreateCDValid(String[] transactionArguments) {
        return transactionArguments[1].equalsIgnoreCase("cd")
                && bank.isIDValid(transactionArguments[2])
                && bank.isAPRValid(Double.parseDouble(transactionArguments[3]))
                && bank.isInitialCDBalanceValid(Double.parseDouble(transactionArguments[4]));
    }
}
