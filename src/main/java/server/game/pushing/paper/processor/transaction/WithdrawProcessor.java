package server.game.pushing.paper.processor.transaction;

import server.game.pushing.paper.TransactionChain;
import server.game.pushing.paper.bank.Bank;

import static java.lang.Double.parseDouble;

public class WithdrawProcessor extends TransactionChain {
    public WithdrawProcessor(Bank bank) {
        super(bank);
    }

    @Override
    public boolean handle(String[] transactionArguments) {
        if (transactionArguments[0].equalsIgnoreCase("withdraw")) {
            bank.withdraw(transactionArguments[1], parseDouble(transactionArguments[2]));
            return true;
        } else {
            return next != null && next.handle(transactionArguments);
        }
    }
}
