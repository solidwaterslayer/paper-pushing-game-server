package server.game.pushing.paper.order_generator;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.game.pushing.paper.store.Store;
import server.game.pushing.paper.store.bank.AccountType;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class OrderGeneratorTests {
    @Test
    protected void order_factories_should_return_a_valid_order() {
        OrderGenerator orderGenerator = new OrderGenerator();
        int size = 9;
        List<String> order;
        List<String> receipt;

        Logger logger = LoggerFactory.getLogger(this.getClass());
        String transaction;

        for (int i = 0; i < 99; i++) {
            order = orderGenerator.getOrder(size, new Random(i));
            assertTrue(order.get(0).contains(AccountType.Checking.name().toLowerCase()));
            assertTrue(order.get(1).contains(AccountType.Checking.name().toLowerCase()));
            assertEquals(size, order.size());

            Store store = new Store();
            store.getOrder().addAll(order);
            receipt = store.getReceipt();
            for (int j = 0; j < receipt.size(); j++) {
                transaction = receipt.get(j);
                logger.info(String.format("[%s] %s", j, transaction));
                assertFalse(transaction.contains("[invalid]"));
            }
        }
    }
}
