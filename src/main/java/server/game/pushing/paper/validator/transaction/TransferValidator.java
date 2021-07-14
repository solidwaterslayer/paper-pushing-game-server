package server.game.pushing.paper.validator.transaction;

import server.game.pushing.paper.bank.Bank;

public class TransferValidator extends TransactionValidator {
    public TransferValidator(Bank bank) {
        super(bank);
    }

    public boolean validate(String[] commandArguments) {
        if (isNumberOfCommandArgumentsInvalid(commandArguments)) {
            return false;
        }

        String commandFromID = commandArguments[1];
        String commandToID = commandArguments[2];
        String commandTransferAmount = commandArguments[3];

        return validateCommandID(commandFromID, commandToID) && validateCommandTransferAmount(commandFromID, commandToID, commandTransferAmount);
    }

    private boolean isNumberOfCommandArgumentsInvalid(String[] commandArguments) {
        return commandArguments.length != 4;
    }

    private boolean validateCommandID(String commandFromID, String commandToID) {
        return isCommandIDDuplicate(commandFromID) && isCommandIDDuplicate(commandToID) && !commandFromID.equals(commandToID);
    }

    private boolean validateCommandTransferAmount(String commandFromID, String commandToID, String commandTransferAmount) {
        return validateCommandWithdrawAmount(commandTransferAmount, commandFromID) && validateCommandDepositAmount(commandTransferAmount, commandToID);
    }
}
