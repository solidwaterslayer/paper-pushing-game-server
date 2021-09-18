package server.game.pushing.paper.ledgervalidator.chainofresponsibility.transactionprocessor;

import server.game.pushing.paper.ledgervalidator.bank.Bank;
import server.game.pushing.paper.ledgervalidator.chainofresponsibility.ChainOfResponsibility;
import server.game.pushing.paper.ledgervalidator.chainofresponsibility.TransactionType;

import static java.lang.Integer.parseInt;

public class PassTimeProcessor extends ChainOfResponsibility {
    public PassTimeProcessor(Bank bank) {
        super(bank);
        transactionType = TransactionType.PassTime;
    }

    @Override
    public boolean handle(String[] transactionArguments) {
        if ((transactionArguments[0] + transactionArguments[1]).equalsIgnoreCase(transactionType.name())) {
            bank.passTime(parseInt(transactionArguments[2]));
            return true;
        }

        return next != null && next.handle(transactionArguments);
    }
}
