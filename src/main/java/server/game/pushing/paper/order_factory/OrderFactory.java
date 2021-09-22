package server.game.pushing.paper.order_factory;

import server.game.pushing.paper.order_factory.transaction_factory.CreateFactory;
import server.game.pushing.paper.order_factory.transaction_factory.TransactionFactory;
import server.game.pushing.paper.store.bank.Bank;
import server.game.pushing.paper.store.chain_of_responsibility.ChainOfResponsibility;
import server.game.pushing.paper.store.chain_of_responsibility.ChainOfResponsibilityFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class OrderFactory {
    public List<String> getOrder(double seed) {
        List<String> order = new ArrayList<>();
        Bank bank = new Bank();
        Random random = new Random((long) seed);
        List<TransactionFactory> transactionFactories;
        ChainOfResponsibilityFactory chainOfResponsibilityFactory = new ChainOfResponsibilityFactory(bank);
        ChainOfResponsibility processor = chainOfResponsibilityFactory.getChainOfResponsibility(false);

        for (int i = 0; i < random.nextInt(9) + 1; i++) {
            transactionFactories = new ArrayList<>();
            transactionFactories.add(new CreateFactory(bank, random));
            // TODO
//            transactionFactories.add(new PassTimeFactory(bank, random));

            if (bank.size() > 0) {
//                transactionFactories.add(new DepositFactory(bank, random));
//                transactionFactories.add(new WithdrawFactory(bank, random));
            }
            if (bank.size() > 1) {
//                transactionFactories.add(new TransferFactory(bank, random));
            }

            String transaction = transactionFactories.get(random.nextInt(transactionFactories.size())).getTransaction();
            processor.handle(transaction);
            order.add(transaction);
        }

        return order;
    }
}
