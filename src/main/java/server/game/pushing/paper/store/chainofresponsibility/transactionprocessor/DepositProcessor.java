package server.game.pushing.paper.store.chainofresponsibility.transactionprocessor;

import server.game.pushing.paper.store.bank.Bank;
import server.game.pushing.paper.store.chainofresponsibility.ChainOfResponsibility;
import server.game.pushing.paper.store.chainofresponsibility.TransactionType;

public class DepositProcessor extends ChainOfResponsibility {
    public DepositProcessor(Bank bank) {
        super(bank);
        transactionType = TransactionType.Deposit;
    }

    @Override
    public boolean handle(String[] transactionArguments) {
        if (transactionArguments[0].equalsIgnoreCase(transactionType.name())) {
            bank.deposit(transactionArguments[1], Double.parseDouble(transactionArguments[2]));
            return true;
        }

        return next != null && next.handle(transactionArguments);
    }
}
