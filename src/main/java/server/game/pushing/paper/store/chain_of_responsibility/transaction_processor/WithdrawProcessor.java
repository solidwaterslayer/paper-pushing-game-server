package server.game.pushing.paper.store.chain_of_responsibility.transaction_processor;

import server.game.pushing.paper.store.bank.Bank;
import server.game.pushing.paper.store.chain_of_responsibility.ChainOfResponsibility;
import server.game.pushing.paper.store.chain_of_responsibility.TransactionType;

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
