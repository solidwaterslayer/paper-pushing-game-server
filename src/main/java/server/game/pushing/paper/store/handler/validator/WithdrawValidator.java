package server.game.pushing.paper.store.handler.validator;

import server.game.pushing.paper.store.bank.Bank;
import server.game.pushing.paper.store.handler.Handler;

import static java.lang.Double.parseDouble;
import static server.game.pushing.paper.store.handler.TransactionType.Withdraw;

public class WithdrawValidator extends Handler {
    public WithdrawValidator(Bank bank) {
        super(bank);
        transactionType = Withdraw;
    }

    @Override
    public boolean handleTransaction(String[] transactionArguments) {
        try {
            if (transactionArguments[0].equalsIgnoreCase(transactionType.name()) && bank.isWithdrawAmountValid(
                    transactionArguments[1],
                    parseDouble(transactionArguments[2])
            )) {
                return true;
            } else {
                return next != null && next.handleTransaction(transactionArguments);
            }
        } catch (ArrayIndexOutOfBoundsException | NumberFormatException exception) {
            return false;
        }
    }
}
