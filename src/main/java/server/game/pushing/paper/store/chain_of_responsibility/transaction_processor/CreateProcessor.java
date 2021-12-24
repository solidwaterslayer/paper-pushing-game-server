package server.game.pushing.paper.store.chain_of_responsibility.transaction_processor;

import server.game.pushing.paper.store.bank.Bank;
import server.game.pushing.paper.store.bank.AccountType;
import server.game.pushing.paper.store.chain_of_responsibility.ChainOfResponsibility;
import server.game.pushing.paper.store.chain_of_responsibility.TransactionType;

import static server.game.pushing.paper.store.bank.AccountType.Checking;
import static server.game.pushing.paper.store.bank.AccountType.Savings;

public class CreateProcessor extends ChainOfResponsibility {
    public CreateProcessor(Bank bank) {
        super(bank);
        transactionType = TransactionType.Create;
    }

    @Override
    public boolean handle(String[] transactionArguments) {
        if (transactionArguments[0].equalsIgnoreCase(transactionType.name()) && handleSecondArgument(transactionArguments)) {
            return true;
        }

        return next != null && next.handle(transactionArguments);
    }

    private boolean handleSecondArgument(String[] transactionArguments) {
        if (transactionArguments[1].equalsIgnoreCase(Checking.toString())) {
            bank.createCheckingAccount(transactionArguments[2]);
        } else if (transactionArguments[1].equalsIgnoreCase(Savings.toString())) {
            bank.createSavingsAccount(transactionArguments[2]);
        } else {
            bank.createCDAccount(transactionArguments[2], parseDouble(transactionArguments[3]));
        }

        return true;
    }
}
