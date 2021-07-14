package server.game.pushing.paper.validator.transaction;

import server.game.pushing.paper.bank.Bank;

import java.util.regex.Pattern;

public class PassValidator extends TransactionValidator {
    public PassValidator(Bank bank) {
        super(bank);
    }

    public boolean validate(String[] commandArguments) {
        String commandTime = commandArguments[1];

        return validateCommandTime(commandTime);
    }

    private boolean validateCommandTime(String commandTime) {
        return isCommandTimeAnInteger(commandTime) && isCommandTimeValid(commandTime);
    }

    private boolean isCommandTimeAnInteger(String commandTime) {
        return Pattern.compile("\\d+").matcher(commandTime).matches();
    }

    private boolean isCommandTimeValid(String commandTime) {
        return bank.isPassTimeValid(Integer.parseInt(commandTime));
    }
}
