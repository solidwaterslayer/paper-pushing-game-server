package server.game.pushing.paper.store.handler.processor;

import server.game.pushing.paper.store.bank.Bank;
import server.game.pushing.paper.store.handler.Handler;

import static server.game.pushing.paper.store.handler.TransactionType.Deposit;

public class DepositProcessor extends Handler {
    public DepositProcessor(Bank bank) {
        super(bank);
        transactionType = Deposit;
    }

    @Override
    public boolean handleTransaction(String[] transactionArguments) {
        if (transactionArguments[0].equalsIgnoreCase(transactionType.name())) {
            bank.deposit(transactionArguments[1], Double.parseDouble(transactionArguments[2]));
            return true;
        }

        return next != null && next.handleTransaction(transactionArguments);
    }
}
