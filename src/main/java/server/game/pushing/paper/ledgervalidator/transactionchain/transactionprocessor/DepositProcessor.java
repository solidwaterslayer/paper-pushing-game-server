package server.game.pushing.paper.ledgervalidator.transactionchain.transactionprocessor;

import server.game.pushing.paper.ledgervalidator.bank.Bank;
import server.game.pushing.paper.ledgervalidator.transactionchain.TransactionChain;
import server.game.pushing.paper.ledgervalidator.transactionchain.TransactionType;

public class DepositProcessor extends TransactionChain {
    public DepositProcessor(Bank bank) {
        super(bank);
        transactionType = TransactionType.Deposit;
    }

    @Override
    public boolean handle(String[] transactionArguments) {
        if (transactionArguments[0].equalsIgnoreCase(transactionType.name())) {
            bank.deposit(transactionArguments[1], Double.parseDouble(transactionArguments[2]));
            return true;
        } else {
            return next != null && next.handle(transactionArguments);
        }
    }
}
