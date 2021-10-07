package server.game.pushing.paper.game_level;

import server.game.pushing.paper.order_factory.OrderFactory;
import server.game.pushing.paper.store.Store;

import java.util.List;
import java.util.Random;

public class GameLevel {
    public List<String> order;
    public List<String> transformedReceipt;
    public List<String> receiptTransformation;

    public GameLevel() {
        OrderFactory orderFactory = new OrderFactory();
        order = orderFactory.getOrder(9, new Random());

        Store store = new Store();
        store.getOrder().addAll(order);
        List<String> receipt = store.getReceipt();
        transformedReceipt = receipt;
        receiptTransformation = receipt;

        // typo
        // change transaction type
        // change account type
        // increment id
        // increment amount
        // missing transaction [not is empty]
        // switch 2 transactions
        // move transaction to invalid

        // amount underload [create and time travel]
        // amount overload
        // withdraw from savings twice
        // withdraw from cd before time traveling 12 months
    }
}
