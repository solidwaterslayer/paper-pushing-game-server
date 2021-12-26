package server.game.pushing.paper.store.handler.processor;

import server.game.pushing.paper.store.bank.Bank;
import server.game.pushing.paper.store.handler.Handler;

import static server.game.pushing.paper.store.bank.AccountType.*;
import static server.game.pushing.paper.store.handler.TransactionType.Create;

public class CreateProcessor extends Handler {
    public CreateProcessor(Bank bank) {
        super(bank);
        transactionType = Create;
    }

    @Override
    public boolean handleTransaction(String[] transactionArguments) {
        if (handleCheckingTransaction(transactionArguments) || handleSavingsTransaction(transactionArguments) || handleCDTransaction(transactionArguments)) {
            return true;
        }

        return next != null && next.handleTransaction(transactionArguments);
    }

    private boolean handleCheckingTransaction(String[] transactionArguments) {
        if (transactionArguments[0].equalsIgnoreCase(transactionType.name()) && transactionArguments[1].equalsIgnoreCase(Checking.name())) {
            bank.createCheckingAccount(transactionArguments[2]);
            return true;
        }

        return false;
    }

    private boolean handleSavingsTransaction(String[] transactionArguments) {
        if (transactionArguments[0].equalsIgnoreCase(transactionType.name()) && transactionArguments[1].equalsIgnoreCase(Savings.name())) {
            bank.createSavingsAccount(transactionArguments[2]);
            return true;
        }

        return false;
    }

    private boolean handleCDTransaction(String[] transactionArguments) {
        if (transactionArguments[0].equalsIgnoreCase(transactionType.name()) && transactionArguments[1].equalsIgnoreCase(CD.name())) {
            bank.createCDAccount(transactionArguments[2], parseDouble(transactionArguments[3]));
            return true;
        }

        return false;
    }
}
