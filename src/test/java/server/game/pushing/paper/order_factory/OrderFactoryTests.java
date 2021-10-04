package server.game.pushing.paper.order_factory;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.game.pushing.paper.store.Store;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class OrderFactoryTests {
    @Test
    protected void order_factories_should_return_a_valid_order() {
        OrderFactory orderFactory = new OrderFactory();
        int size = 9;
        Store store = new Store();

        Logger logger = LoggerFactory.getLogger(this.getClass());

        for (int i = 0; i < 99; i++) {
            List<String> order = orderFactory.getOrder(i, size);

            store.setOrder(order);

            for (String transaction : store.getReceipt()) {
                logger.info(String.format("[order factory test %s] %s", i, transaction));
                assertFalse(transaction.contains("[invalid]"));
            }
            assertEquals(size, order.size());
        }
    }
}
