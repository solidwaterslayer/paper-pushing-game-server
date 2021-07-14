package server.game.pushing.paper.validator.transaction;

import server.game.pushing.paper.bank.Bank;

public class CreateValidator extends TransactionValidator {
    public CreateValidator(Bank bank) {
        super(bank);
    }

    public boolean validate(String[] commandArguments) {
        String commandAccountType = commandArguments[1].toLowerCase();

        return !isNumberOfCommandArgumentsInvalid(commandArguments, commandAccountType) && validateCommandAccountType(commandAccountType) && validateCommandID(commandArguments[2]) && validateCommandAPR(commandArguments[3]) && validateCreateCDCommandBalance(commandArguments, commandAccountType);
    }

    private boolean isNumberOfCommandArgumentsInvalid(String[] commandArguments, String commandType) {
        return (!commandType.equals("cd") || commandArguments.length != 5) && (commandType.equals("cd") || commandArguments.length != 4);
    }

    private boolean validateCommandAccountType(String commandAccountType) {
        return isCommandAccountTypeValid(commandAccountType);
    }

    private boolean isCommandAccountTypeValid(String commandType) {
//        for (Account.Type type : Account.Type.values()) {
//            String aValidType = type.name().toLowerCase();
//
//            if (commandType.equals(aValidType)) {
//                return true;
//            }
//        }
//
        return false;
    }

    private boolean validateCommandAPR(String commandAPR) {
        return isCommandArgumentARealNumber(commandAPR) && isCommandAPRValid(commandAPR);
    }

    private boolean isCommandAPRValid(String commandAPR) {
        return bank.isAPRValid(Double.parseDouble(commandAPR));
    }

    private boolean validateCreateCDCommandBalance(String[] commandArguments, String commandType) {
        if (!commandType.equals("cd")) {
            return true;
        }

        String commandBalance = commandArguments[4];

        return isCommandArgumentARealNumber(commandBalance) && isCreateCDCommandBalanceValid(commandBalance);
    }

    private boolean isCreateCDCommandBalanceValid(String commandBalance) {
        return bank.isInitialCDBalanceValid(Double.parseDouble(commandBalance));
    }
}
