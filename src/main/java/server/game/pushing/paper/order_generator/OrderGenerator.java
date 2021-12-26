package server.game.pushing.paper.order_generator;

import server.game.pushing.paper.order_generator.transaction_generator.*;
import server.game.pushing.paper.store.bank.AccountType;
import server.game.pushing.paper.store.bank.Bank;
import server.game.pushing.paper.store.handler.ChainOfResponsibilityFactory;
import server.game.pushing.paper.store.handler.Handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class OrderGenerator {
    private Random random;
    private List<TransactionGenerator> transactionFactories;
    private Handler processor;
    private List<String> order;

    public List<String> getOrder(int size, Random random) {
        Bank bank = new Bank();
        this.random = random;
        transactionFactories = Arrays.asList(new CreateGenerator(bank, random), new DepositGenerator(bank, random), new WithdrawGenerator(bank, random), new TransferGenerator(bank, random), new TimeTravelGenerator(bank, random));
        processor = (new ChainOfResponsibilityFactory(bank)).getProcessor();
        order = new ArrayList<>();

        addAllTransactions(size, 2);

        return order;
    }

    private void addAllTransactions(int size, int createWeight) {
        if (createWeight != 0) {
            addTransaction(((CreateGenerator) transactionFactories.get(0)).getLoadedTransaction(AccountType.Checking));
            addAllTransactions(size - 1, createWeight - 1);
        } else if (size > 0) {
            addTransaction(transactionFactories.get(random.nextInt(transactionFactories.size())).getTransaction());
            addAllTransactions(size - 1, 0);
        }
    }

    private void addTransaction(String transaction) {
        processor.handleTransaction(transaction);
        order.add(transaction);
    }
}
