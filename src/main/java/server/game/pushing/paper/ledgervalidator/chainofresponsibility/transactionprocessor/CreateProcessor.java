package server.game.pushing.paper.ledgervalidator.chainofresponsibility.transactionprocessor;

import server.game.pushing.paper.ledgervalidator.bank.Bank;
import server.game.pushing.paper.ledgervalidator.chainofresponsibility.ChainOfResponsibility;
import server.game.pushing.paper.ledgervalidator.chainofresponsibility.TransactionType;

import java.util.Objects;

import static java.lang.Double.parseDouble;
import static server.game.pushing.paper.ledgervalidator.bank.account.AccountType.parseAccountType;

public class CreateProcessor extends ChainOfResponsibility {
    public CreateProcessor(Bank bank) {
        super(bank);
        transactionType = TransactionType.Create;
    }

    @Override
    public boolean handle(String[] transactionArguments) {
        if (transactionArguments[0].equalsIgnoreCase(transactionType.name())) {
            switch (Objects.requireNonNull(parseAccountType(transactionArguments[1]))) {
                case Checking:
                    bank.createChecking(transactionArguments[2], parseDouble(transactionArguments[3]));
                    return true;
                case Savings:
                    bank.createSavings(transactionArguments[2], parseDouble(transactionArguments[3]));
                    return true;
                default:
                    bank.createCD(transactionArguments[2], parseDouble(transactionArguments[3]), parseDouble(transactionArguments[4]));
                    return true;
            }
        } else {
            return next != null && next.handle(transactionArguments);
        }
    }
}
