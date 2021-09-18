package server.game.pushing.paper.ledgervalidator.chainofresponsibility.transactionvalidator;

import server.game.pushing.paper.ledgervalidator.bank.Bank;
import server.game.pushing.paper.ledgervalidator.chainofresponsibility.ChainOfResponsibility;
import server.game.pushing.paper.ledgervalidator.chainofresponsibility.TransactionType;

import static java.lang.Double.parseDouble;

public class CreateValidator extends ChainOfResponsibility {
    public CreateValidator(Bank bank) {
        super(bank);
        transactionType = TransactionType.Create;
    }

    @Override
    public boolean handle(String[] transactionArguments) {
        try {
            // TODO: fix mee
            if (transactionArguments[0].equalsIgnoreCase(transactionType.name())
                    && (handleCreateCheckingTransaction(transactionArguments)
                    || handleCreateSavingsTransaction(transactionArguments)
                    || handleCreateCDTransaction(transactionArguments))) {
                return true;
            } else {
                return next != null && next.handle(transactionArguments);
            }
        } catch (ArrayIndexOutOfBoundsException | NumberFormatException exception) {
            return false;
        }
    }

    private boolean handleCreateCheckingTransaction(String[] transactionArguments) {
        // TODO: fix mee
        return transactionArguments[1].equalsIgnoreCase("checking")
                && bank.isIDValid(transactionArguments[2])
                && bank.isAPRValid(parseDouble(transactionArguments[3]));
    }

    private boolean handleCreateSavingsTransaction(String[] transactionArguments) {
        return transactionArguments[1].equalsIgnoreCase("savings")
                && bank.isIDValid(transactionArguments[2])
                && bank.isAPRValid(parseDouble(transactionArguments[3]));
    }

    private boolean handleCreateCDTransaction(String[] transactionArguments) {
        return transactionArguments[1].equalsIgnoreCase("cd")
                && bank.isIDValid(transactionArguments[2])
                && bank.isAPRValid(parseDouble(transactionArguments[3]))
                && bank.isInitialCDBalanceValid(parseDouble(transactionArguments[4]));
    }
}
