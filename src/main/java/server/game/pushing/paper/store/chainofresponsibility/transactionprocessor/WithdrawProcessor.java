package server.game.pushing.paper.store.chainofresponsibility.transactionprocessor;

import server.game.pushing.paper.store.bank.Bank;
import server.game.pushing.paper.store.chainofresponsibility.ChainOfResponsibility;
import server.game.pushing.paper.store.chainofresponsibility.TransactionType;

import static java.lang.Double.parseDouble;

public class WithdrawProcessor extends ChainOfResponsibility {
    public WithdrawProcessor(Bank bank) {
        super(bank);
        transactionType = TransactionType.Withdraw;
    }

    @Override
    public boolean handle(String[] transactionArguments) {
        if (transactionArguments[0].equalsIgnoreCase(transactionType.name())) {
            bank.withdraw(transactionArguments[1], parseDouble(transactionArguments[2]));
            return true;
        }

        return next != null && next.handle(transactionArguments);
    }
}
