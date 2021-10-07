package server.game.pushing.paper.game_level;

import server.game.pushing.paper.invalid_receipt_factory.InvalidReceiptFactory;
import server.game.pushing.paper.order_factory.OrderFactory;

import java.util.List;
import java.util.Random;

public class GameLevel {
    public List<String> order;
    public List<String> invalidReceipt;
    public List<String> receiptTransformation;

    public GameLevel() {
        OrderFactory orderFactory = new OrderFactory();
        order = orderFactory.getOrder(9, new Random());

        InvalidReceiptFactory invalidReceiptFactory = new InvalidReceiptFactory();
        invalidReceipt = invalidReceiptFactory.getInvalidReceipt(order);
        receiptTransformation = invalidReceiptFactory.getReceiptTransformation();
    }
}
