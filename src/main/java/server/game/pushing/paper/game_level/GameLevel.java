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
        order = orderGenerator.getOrder(9, random);
        Store store = new Store();
        store.getOrder().addAll(order);
        receipt = store.getReceipt();
        transformation = new ArrayList<>();

        // typo
            // change transaction type
            // change account type
            // increment id
            // increment amount
        // move
        // switch valid
        // remove

        // overload
        // paradox
            // withdraw from savings twice
            // withdraw from cd before time traveling 12 months
//        List<String> errors = new ArrayList<>(Arrays.asList("typo", "move", "switch", "remove", "exceedBalance", "exceedTime"));
    }
}
