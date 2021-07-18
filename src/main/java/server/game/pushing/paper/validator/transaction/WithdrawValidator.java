package server.game.pushing.paper.validator.transaction;

import server.game.pushing.paper.TransactionChain;
import server.game.pushing.paper.bank.Bank;

import static java.lang.Double.parseDouble;

public class WithdrawValidator extends TransactionChain {
    public WithdrawValidator(Bank bank) {
        super(bank);
    }

    @Override
    public boolean handle(String[] transactionArguments) {
        try {
            if (transactionArguments[0].equalsIgnoreCase("withdraw") && bank.isWithdrawValid(transactionArguments[1], parseDouble(transactionArguments[2]))) {
                return true;
            } else {
                return next != null && next.handle(transactionArguments);
            }
        } catch (ArrayIndexOutOfBoundsException | NumberFormatException exception) {
            return false;
        }
    }
}
