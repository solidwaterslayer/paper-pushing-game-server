package server.game.pushing.paper.store.chainofresponsibility.transactionvalidator;

import server.game.pushing.paper.store.bank.Bank;
import server.game.pushing.paper.store.chainofresponsibility.ChainOfResponsibility;
import server.game.pushing.paper.store.chainofresponsibility.TransactionType;

import static java.lang.Integer.parseInt;

public class PassTimeValidator extends ChainOfResponsibility {
    public PassTimeValidator(Bank bank) {
        super(bank);
        transactionType = TransactionType.PassTime;
    }

    @Override
    public boolean handle(String[] transactionArguments) {
        try {
            if ((transactionArguments[0] + transactionArguments[1]).equalsIgnoreCase(transactionType.name())
                    && bank.isPassTimeValid(parseInt(transactionArguments[2]))) {
                return true;
            } else {
                return next != null && next.handle(transactionArguments);
            }
        } catch (ArrayIndexOutOfBoundsException | NumberFormatException exception) {
            return false;
        }
    }
}