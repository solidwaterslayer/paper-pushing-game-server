package server.game.pushing.paper.ledgervalidator.transactionchain.transactionvalidator;

import server.game.pushing.paper.ledgervalidator.bank.Bank;
import server.game.pushing.paper.ledgervalidator.transactionchain.TransactionChain;
import server.game.pushing.paper.ledgervalidator.transactionchain.TransactionType;

import static java.lang.Double.parseDouble;

public class DepositValidator extends TransactionChain {
    public DepositValidator(Bank bank) {
        super(bank);
        transactionType = TransactionType.Deposit;
    }

    @Override
    public boolean handle(String[] transactionArguments) {
        try {
            if (transactionArguments[0].equalsIgnoreCase(transactionType.name())
                    && bank.isDepositAmountValid(transactionArguments[1], parseDouble(transactionArguments[2]))) {
                return true;
            } else {
                return next != null && next.handle(transactionArguments);
            }
        } catch (ArrayIndexOutOfBoundsException | NumberFormatException exception) {
            return false;
        }
    }
}
