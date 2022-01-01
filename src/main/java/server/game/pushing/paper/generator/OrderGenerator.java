package server.game.pushing.paper.generator;

import server.game.pushing.paper.store.bank.Bank;
import server.game.pushing.paper.store.handler.ChainOfResponsibility;
import server.game.pushing.paper.store.handler.Handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static server.game.pushing.paper.store.bank.AccountType.Checking;

public class OrderGenerator {
    public List<String> generateOrder(Random random, int size) {
        List<String> order = new ArrayList<>();
        Bank bank = new Bank();
        List<Generator> generators = new ArrayList<>(Arrays.asList(
                new CreateGenerator(random, bank),
                new TimeTravelGenerator(random, bank),
                new DepositGenerator(random, bank),
                new WithdrawGenerator(random, bank),
                new TransferGenerator(random, bank)
        ));
        Handler processors = (new ChainOfResponsibility(bank)).getProcessors();

        for (int i = 0; i < size; i++) {
            String transaction = ((CreateGenerator) generators.get(0)).generateTransaction(Checking);
            if (i >= 2) {
                transaction = generators.get(random.nextInt(generators.size())).generateTransaction();
            }
            processors.handleTransaction(transaction);
            order.add(transaction);
        }

        return order;
    }
}
