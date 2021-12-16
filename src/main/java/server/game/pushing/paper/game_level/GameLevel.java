package server.game.pushing.paper.game_level;

import server.game.pushing.paper.order_generator.OrderGenerator;
import server.game.pushing.paper.store.Store;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameLevel {
    public List<String> order;
    public List<String> receipt;
    public List<String> transformation;

    public GameLevel() {
        OrderGenerator orderGenerator = new OrderGenerator();
        Random random = new Random();
        order = orderGenerator.getOrder(6, random);
        Store store = new Store();
        store.getOrder().addAll(order);
        receipt = store.getReceipt();
        transformation = new ArrayList<>();
    }
}
