package server.game.pushing.paper.game_level;

import server.game.pushing.paper.order_generator.OrderGenerator;
import server.game.pushing.paper.store.Store;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class GameLevel {
    public List<String> order;
    public List<String> receipt;
    public List<String> mutation;

    public GameLevel() {
        OrderGenerator orderGenerator = new OrderGenerator();
        Random random = new Random();
        order = orderGenerator.getOrder(6, random);
        Store store = new Store();
        store.getOrder().addAll(order);
        receipt = store.getReceipt();
        mutation = new ArrayList<>();

        List<Integer> mutationLocations = new ArrayList<>();
        while (mutationLocations.size() < 2) {
            int mutationLocation = random.nextInt(receipt.size());
            if (!receipt.get(mutationLocation).equals("") & !mutationLocations.contains(mutationLocation)) {
                mutationLocations.add(mutationLocation);
            }
        }

        List<Mutation> possibleMutations = new ArrayList<>(Arrays.asList(Mutation.TYPO, Mutation.TYPO, Mutation.MOVE));
        int i = random.nextInt(possibleMutations.size());
        Mutation mutation0 = possibleMutations.get(i);
        possibleMutations.remove(i);
        Mutation mutation1 = possibleMutations.get(random.nextInt(possibleMutations.size()));
    }
}
