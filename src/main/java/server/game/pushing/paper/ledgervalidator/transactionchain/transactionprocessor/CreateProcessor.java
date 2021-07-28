package server.game.pushing.paper.ledgervalidator.transactionchain.transactionprocessor;

import server.game.pushing.paper.ledgervalidator.bank.Bank;
import server.game.pushing.paper.ledgervalidator.bank.account.AccountType;
import server.game.pushing.paper.ledgervalidator.transactionchain.TransactionChain;
import server.game.pushing.paper.ledgervalidator.transactionchain.TransactionType;

import static java.lang.Double.parseDouble;

public class CreateProcessor extends TransactionChain {
    public CreateProcessor(Bank bank) {
        super(bank);
        transactionType = TransactionType.Create;
    }

    @Override
    public boolean handle(String[] transactionArguments) {
        if (transactionArguments[0].equalsIgnoreCase(transactionType.name())) {
            return handleCreateCheckingTransaction(transactionArguments)
                    || handleCreateSavingsTransaction(transactionArguments)
                    || handleCreateCDTransaction(transactionArguments);
        } else {
            return next != null && next.handle(transactionArguments);
        }
    }

    protected boolean handleCreateCheckingTransaction(String[] transactionArguments) {
        boolean isTransactionValid = transactionArguments[1].equalsIgnoreCase(AccountType.Checking.name());
        if (isTransactionValid) {
            bank.createChecking(transactionArguments[2], parseDouble(transactionArguments[3]));
        }
        return isTransactionValid;
    }

    protected boolean handleCreateSavingsTransaction(String[] transactionArguments) {
        boolean isTransactionValid = transactionArguments[1].equalsIgnoreCase(AccountType.Savings.name());
        if (isTransactionValid) {
            bank.createSavings(transactionArguments[2], parseDouble(transactionArguments[3]));
        }
        return isTransactionValid;
    }

    protected boolean handleCreateCDTransaction(String[] transactionArguments) {
        boolean isTransactionValid = transactionArguments[1].equalsIgnoreCase(AccountType.CD.name());
        if (isTransactionValid) {
            bank.createCD(transactionArguments[2], parseDouble(transactionArguments[3]), parseDouble(transactionArguments[4]));
        }
        return isTransactionValid;
    }
}
