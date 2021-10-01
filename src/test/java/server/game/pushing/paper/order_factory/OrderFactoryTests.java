package server.game.pushing.paper.order_factory;

import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.game.pushing.paper.store.Store;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;

public class OrderFactoryTests {
    @Test
    protected void get_order_should_return_a_valid_and_random_order() {
        OrderFactory orderFactory = new OrderFactory();
        Store store = new Store();

        Logger logger = LoggerFactory.getLogger(this.getClass());

        for (int i = 0; i < 99; i++) {
            store.setOrder(orderFactory.getOrder(i));
            List<String> receipt = store.getReceipt();

            for (String transaction : receipt) {
                logger.info(String.format("[order factory test %s] %s", i, transaction));
                assertFalse(transaction.contains("[invalid]"));
            }
        }
    }
}
