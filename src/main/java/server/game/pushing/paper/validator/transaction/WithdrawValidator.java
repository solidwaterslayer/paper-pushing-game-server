package server.game.pushing.paper.validator.transaction;

import server.game.pushing.paper.bank.Bank;

public class WithdrawValidator extends TransactionValidator {
    public WithdrawValidator(Bank bank) {
        super(bank);
    }

    public boolean validate(String[] commandArguments) {
        return validateDepositAndWithdrawArguments(commandArguments) && validateCommandWithdrawAmount(commandArguments[2], commandArguments[1]);
    }
}
