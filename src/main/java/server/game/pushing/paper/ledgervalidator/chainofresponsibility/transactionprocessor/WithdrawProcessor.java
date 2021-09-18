package server.game.pushing.paper.ledgervalidator.chainofresponsibility.transactionprocessor;

import server.game.pushing.paper.ledgervalidator.bank.Bank;
import server.game.pushing.paper.ledgervalidator.chainofresponsibility.ChainOfResponsibility;
import server.game.pushing.paper.ledgervalidator.chainofresponsibility.TransactionType;

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
        } else {
            return next != null && next.handle(transactionArguments);
        }
    }
}
