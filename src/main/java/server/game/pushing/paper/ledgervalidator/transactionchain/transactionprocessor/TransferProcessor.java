package server.game.pushing.paper.ledgervalidator.transactionchain.transactionprocessor;

import server.game.pushing.paper.ledgervalidator.bank.Bank;
import server.game.pushing.paper.ledgervalidator.transactionchain.TransactionChain;
import server.game.pushing.paper.ledgervalidator.transactionchain.TransactionType;

import static java.lang.Double.parseDouble;

public class TransferProcessor extends TransactionChain {
    public TransferProcessor(Bank bank) {
        super(bank);
        transactionType = TransactionType.Transfer;
    }

    @Override
    public boolean handle(String[] transactionArguments) {
        if (transactionArguments[0].equalsIgnoreCase(transactionType.name())) {
            bank.transfer(transactionArguments[1], transactionArguments[2], parseDouble(transactionArguments[3]));
            return true;
        } else {
            return next != null && next.handle(transactionArguments);
        }
    }
}
