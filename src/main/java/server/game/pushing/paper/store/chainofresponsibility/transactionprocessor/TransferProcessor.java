package server.game.pushing.paper.store.chainofresponsibility.transactionprocessor;

import server.game.pushing.paper.store.bank.Bank;
import server.game.pushing.paper.store.chainofresponsibility.ChainOfResponsibility;
import server.game.pushing.paper.store.chainofresponsibility.TransactionType;

import static java.lang.Double.parseDouble;

public class TransferProcessor extends ChainOfResponsibility {
    public TransferProcessor(Bank bank) {
        super(bank);
        transactionType = TransactionType.Transfer;
    }

    @Override
    public boolean handle(String[] transactionArguments) {
        if (transactionArguments[0].equalsIgnoreCase(transactionType.name())) {
            bank.transfer(transactionArguments[1], transactionArguments[2], parseDouble(transactionArguments[3]));
            return true;
        }

        return next != null && next.handle(transactionArguments);
    }
}