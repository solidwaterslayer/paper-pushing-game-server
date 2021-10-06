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
    private List<String> order;
    private List<TransactionFactory> transactionFactories;
    private ChainOfResponsibility processor;

    public List<String> getOrder(int size, Random random) {
        order = new ArrayList<>();
        Bank bank = new Bank();
        transactionFactories = Arrays.asList(new CreateFactory(bank, random), new DepositFactory(bank, random), new WithdrawFactory(bank, random), new TransferFactory(bank, random), new TimeTravelFactory(bank, random));
        processor = (new ChainOfResponsibilityFactory(bank)).getChainOfResponsibility(false);

        addAllTransactions(size, random, 2);

        return order;
    }

    private void addAllTransactions(int size, Random random, int weight) {
        if (weight != 0) {
            addTransaction(((CreateFactory) transactionFactories.get(0)).getLoadedTransaction(AccountType.CHECKING));
            addAllTransactions(size - 1, random, weight - 1);
        } else if (size > 0) {
            addTransaction(transactionFactories.get(random.nextInt(transactionFactories.size())).getTransaction());
            addAllTransactions(size - 1, random, 0);
        }
    }

    private void addTransaction(String transaction) {
        processor.handle(transaction);
        order.add(transaction);
    }
}
