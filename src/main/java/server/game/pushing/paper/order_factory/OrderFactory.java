package server.game.pushing.paper.order_factory;

import server.game.pushing.paper.order_factory.transaction_factory.*;
import server.game.pushing.paper.store.bank.Bank;
import server.game.pushing.paper.store.bank.account.AccountType;
import server.game.pushing.paper.store.chain_of_responsibility.ChainOfResponsibility;
import server.game.pushing.paper.store.chain_of_responsibility.ChainOfResponsibilityFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class OrderFactory {
    public List<String> getOrder(int size, Random random) {
        List<String> order = new ArrayList<>();
        Bank bank = new Bank();
        List<TransactionFactory> transactionFactories = Arrays.asList(new CreateFactory(bank, random), new DepositFactory(bank, random), new WithdrawFactory(bank, random), new TransferFactory(bank, random), new TimeTravelFactory(bank, random));
        ChainOfResponsibility processor = (new ChainOfResponsibilityFactory(bank)).getChainOfResponsibility(false);

        String transaction;
        for (int i = 0; i < size; i++) {
            if (i < 2) {
                transaction = ((CreateFactory) transactionFactories.get(0)).getLoadedTransaction(AccountType.CHECKING);
            } else {
                transaction = transactionFactories.get(random.nextInt(transactionFactories.size())).getTransaction();
            }

            processor.handle(transaction);
            order.add(transaction);
        }

        return order;
    }
}
