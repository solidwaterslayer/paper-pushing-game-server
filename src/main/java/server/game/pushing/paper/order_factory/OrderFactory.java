package server.game.pushing.paper.order_factory;

import server.game.pushing.paper.order_factory.transaction_factory.*;
import server.game.pushing.paper.store.bank.Bank;
import server.game.pushing.paper.store.chain_of_responsibility.ChainOfResponsibility;
import server.game.pushing.paper.store.chain_of_responsibility.ChainOfResponsibilityFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class OrderFactory {
    public List<String> getOrder(int seed, int size) {
        List<String> order = new ArrayList<>();
        Bank bank = new Bank();
        Random random = new Random(seed);
        ChainOfResponsibility processor = (new ChainOfResponsibilityFactory(bank)).getChainOfResponsibility(false);

        for (int i = 0; i < size; i++) {
            List<TransactionFactory> transactionFactories = new ArrayList<>(Arrays.asList(new CreateFactory(bank, random), new TimeTravelFactory(bank, random)));
            if (!transactionFactories.get(0).isException()) {
                transactionFactories.addAll(Arrays.asList(new DepositFactory(bank, random), new WithdrawFactory(bank, random), new TransferFactory(bank, random)));
            }

            String transaction = transactionFactories.get(random.nextInt(transactionFactories.size())).getTransaction();
            processor.handle(transaction);
            order.add(transaction);
        }

        return order;
    }
}
