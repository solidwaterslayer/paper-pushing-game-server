package server.game.pushing.paper.order_factory;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.game.pushing.paper.store.Store;
import server.game.pushing.paper.store.bank.account.AccountType;

import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class OrderFactoryTests {
    @Test
    protected void order_factories_should_return_a_valid_order() {
        OrderFactory orderFactory = new OrderFactory();
        int size = 9;
        Store store = new Store();
        List<String> order;
        List<String> receipt;

        Logger logger = LoggerFactory.getLogger(this.getClass());
        String transaction;

        for (int i = 0; i < 99; i++) {
            order = orderFactory.getOrder(size, new Random(i));
            assertTrue(order.get(0).contains(AccountType.CHECKING.name().toLowerCase()));
            assertTrue(order.get(1).contains(AccountType.CHECKING.name().toLowerCase()));
            assertEquals(size, order.size());

            store.setOrder(order);
            receipt = store.getReceipt();
            for (int j = 0; j < receipt.size(); j++) {
                transaction = receipt.get(j);
                logger.info(String.format("[order factory test %s] %s", j, transaction));
                assertFalse(transaction.contains("[invalid]"));
            }
        }
    }
}
