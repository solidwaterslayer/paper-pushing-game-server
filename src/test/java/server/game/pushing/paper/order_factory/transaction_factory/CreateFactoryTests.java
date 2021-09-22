package server.game.pushing.paper.order_factory.transaction_factory;

import org.junit.jupiter.api.Test;
import server.game.pushing.paper.store.bank.Bank;
import server.game.pushing.paper.store.chain_of_responsibility.ChainOfResponsibility;
import server.game.pushing.paper.store.chain_of_responsibility.ChainOfResponsibilityFactory;

import java.util.HashSet;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CreateFactoryTests {
    @Test
    protected void get_transaction_should_return_a_valid_and_random_transaction() {
        HashSet<String> order = new HashSet<>();
        Bank bank = new Bank();
        Random random = new Random();
        CreateFactory createFactory = new CreateFactory(bank, random);
        ChainOfResponsibilityFactory chainOfResponsibilityFactory = new ChainOfResponsibilityFactory(bank);
        ChainOfResponsibility validator = chainOfResponsibilityFactory.getChainOfResponsibility(true);

        for (int i = 0; i < 9999; i++) {
            String transaction = createFactory.getTransaction();

            assertTrue(validator.handle(transaction));
            assertFalse(order.contains(transaction));

            order.add(transaction);
        }
    }
}
