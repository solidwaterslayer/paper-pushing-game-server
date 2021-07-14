package server.game.pushing.paper.validator.transaction;

import server.game.pushing.paper.bank.Bank;

public class DepositValidator extends TransactionValidator {
    public DepositValidator(Bank bank) {
        super(bank);
    }

    public boolean validate(String[] commandArguments) {
        return validateDepositAndWithdrawArguments(commandArguments) && validateCommandDepositAmount(commandArguments[2], commandArguments[1]);
    }
}
