package server.game.pushing.paper.game_level;

import server.game.pushing.paper.invalid_receipt_generator.InvalidReceiptGenerator;
import server.game.pushing.paper.order_generator.OrderGenerator;

import java.util.List;
import java.util.Random;

public class GameLevel {
    public List<String> order;
    public List<String> invalidReceipt;
    public List<String> receiptTransformation;

    public GameLevel() {
        OrderGenerator orderGenerator = new OrderGenerator();
        order = orderGenerator.getOrder(9, new Random());

        InvalidReceiptGenerator invalidReceiptGenerator = new InvalidReceiptGenerator();
        invalidReceipt = invalidReceiptGenerator.getInvalidReceipt(order);
        receiptTransformation = invalidReceiptGenerator.getReceiptTransformation();
    }
}
