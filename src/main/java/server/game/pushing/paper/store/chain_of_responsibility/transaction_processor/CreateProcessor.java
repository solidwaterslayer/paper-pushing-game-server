package server.game.pushing.paper.store.chain_of_responsibility.transaction_processor;

import server.game.pushing.paper.store.bank.Bank;
import server.game.pushing.paper.store.bank.account.AccountType;
import server.game.pushing.paper.store.chain_of_responsibility.ChainOfResponsibility;
import server.game.pushing.paper.store.chain_of_responsibility.TransactionType;

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
        if (transactionArguments[1].equalsIgnoreCase(AccountType.Checking.name())) {
            bank.createChecking(transactionArguments[2], parseDouble(transactionArguments[3]));
        } else if (transactionArguments[1].equalsIgnoreCase(AccountType.Savings.name())) {
            bank.createSavings(transactionArguments[2], parseDouble(transactionArguments[3]));
        } else {
            bank.createCD(transactionArguments[2], parseDouble(transactionArguments[3]), parseDouble(transactionArguments[4]));
        }

        return true;
    }
}
