package server.game.pushing.paper.store.chainofresponsibility.transactionvalidator;

import server.game.pushing.paper.store.bank.Bank;
import server.game.pushing.paper.store.chainofresponsibility.ChainOfResponsibility;
import server.game.pushing.paper.store.chainofresponsibility.TransactionType;

import static java.lang.Double.parseDouble;

public class TransferValidator extends ChainOfResponsibility {
    public TransferValidator(Bank bank) {
        super(bank);
        transactionType = TransactionType.Transfer;
    }

    @Override
    public boolean handle(String[] transactionArguments) {
        try {
            if (transactionArguments[0].equalsIgnoreCase(transactionType.name())
                    && bank.isTransferAmountValid(transactionArguments[1], transactionArguments[2], parseDouble(transactionArguments[3]))) {
                return true;
            } else {
                return next != null && next.handle(transactionArguments);
            }
        } catch (ArrayIndexOutOfBoundsException | NumberFormatException exception) {
            return false;
        }
    }
}
