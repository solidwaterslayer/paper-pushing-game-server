package server.game.pushing.paper.validator.transaction;

import server.game.pushing.paper.bank.Bank;

import java.util.regex.Pattern;

public class TransactionValidator {
    protected Bank bank;

    public TransactionValidator(Bank bank) {
        this.bank = bank;
    }

    public boolean validate(String transaction) {
        String[] transactionArguments = transaction.split(" ");

        if (transactionArguments.length < 2) {
            return false;
        }

        switch (transactionArguments[0].toLowerCase()) {
            case "create":
                CreateValidator createValidator = new CreateValidator(bank);
                return createValidator.validate(transactionArguments);
            case "deposit":
                DepositValidator depositValidator = new DepositValidator(bank);
                return depositValidator.validate(transactionArguments);
            case "withdraw":
                WithdrawValidator withdrawValidator = new WithdrawValidator(bank);
                return withdrawValidator.validate(transactionArguments);
            case "transfer":
                TransferValidator transferValidator = new TransferValidator(bank);
                return transferValidator.validate(transactionArguments);
            case "pass":
                PassValidator passValidator = new PassValidator(bank);
                return passValidator.validate(transactionArguments);
            default:
                return false;
        }
    }

    protected boolean validateCommandID(String commandID) {
        return isCommandID8Digits(commandID) && !isCommandIDDuplicate(commandID);
    }

    private boolean isCommandID8Digits(String commandID) {
        return commandID.length() == 8 && commandID.matches("[0-9]+");
    }

    protected boolean isCommandIDDuplicate(String commandID) {
        return bank.getAccount(commandID) != null;
    }

    protected boolean validateDepositAndWithdrawArguments(String[] commandArguments) {
        return isNumberOfDepositAndWithdrawArgumentsValid(commandArguments) && isCommandIDDuplicate(commandArguments[1]);
    }

    protected boolean isNumberOfDepositAndWithdrawArgumentsValid(String[] commandArguments) {
        return commandArguments.length == 3;
    }

    protected boolean validateCommandDepositAmount(String commandDepositAmount, String commandID) {
        return isCommandArgumentARealNumber(commandDepositAmount) && isCommandDepositAmountValid(commandDepositAmount, commandID);
    }

    protected boolean isCommandArgumentARealNumber(String commandArgument) {
        return Pattern.compile("-?\\d+(\\.\\d+)?").matcher(commandArgument).matches();
    }

    private boolean isCommandDepositAmountValid(String commandDepositAmount, String commandID) {
        return bank.isDepositValid(commandID, Double.parseDouble(commandDepositAmount));
    }

    protected boolean validateCommandWithdrawAmount(String commandWithdrawAmount, String commandID) {
        return isCommandArgumentARealNumber(commandWithdrawAmount) && isCommandWithdrawAmountValid(commandWithdrawAmount, commandID);
    }

    private boolean isCommandWithdrawAmountValid(String commandWithdrawAmount, String commandID) {
        return bank.isWithdrawValid(commandID, Double.parseDouble(commandWithdrawAmount));
    }
}
