package server.game.pushing.paper.store.handler.processor;

import server.game.pushing.paper.store.bank.Bank;
import server.game.pushing.paper.store.handler.Handler;

import static server.game.pushing.paper.store.handler.TransactionType.Withdraw;

public class WithdrawProcessor extends Handler {
    public WithdrawProcessor(Bank bank) {
        super(bank);
        transactionType = Withdraw;
    }

    @Override
    public boolean handleTransaction(String[] transactionArguments) {
        if (transactionArguments[0].equalsIgnoreCase(transactionType.name())) {
            bank.withdraw(transactionArguments[1], parseDouble(transactionArguments[2]));
            return true;
        }

        return next != null && next.handleTransaction(transactionArguments);
    }
}
