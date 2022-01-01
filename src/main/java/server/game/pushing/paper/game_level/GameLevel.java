package server.game.pushing.paper.game_level;

import server.game.pushing.paper.generator.OrderGenerator;
import server.game.pushing.paper.store.Store;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static java.util.Collections.swap;

public class GameLevel {
    private final Random RANDOM;
    public List<String> order;
    public List<String> receipt;

    public List<String> transformation;
    private final int NUMBER_OF_MUTATIONS = 2;

    public GameLevel() {
        RANDOM = new Random();
        OrderGenerator orderGenerator = new OrderGenerator();
        order = orderGenerator.generateOrder(RANDOM, 6);
        Store store = new Store();
        store.setOrder(order);
        receipt = store.getReceipt();
        transformation = new ArrayList<>();
        for (int i = 0; i < receipt.size(); i++) {
            transformation.add("There is no mutation here.");
        }

        List<Mutation> possibleMutations = new ArrayList<>(Arrays.asList(Mutation.TYPO, Mutation.TYPO, Mutation.DISPLACED));
        List<Mutation> mutations = new ArrayList<>();
        while (mutations.size() < NUMBER_OF_MUTATIONS) {
            int mutationIndex = RANDOM.nextInt((possibleMutations.size()));
            mutations.add(possibleMutations.get(mutationIndex));
            possibleMutations.remove(mutationIndex);
        }
        List<Integer> mutationLocations = new ArrayList<>();
        while (mutationLocations.size() < NUMBER_OF_MUTATIONS) {
            int mutationLocation = RANDOM.nextInt(receipt.size());
            if (!receipt.get(mutationLocation).equals("") & !mutationLocations.contains(mutationLocation)) {
                mutationLocations.add(mutationLocation);
            }
        }
        addMutations(mutations, mutationLocations);
    }

    private void addMutations(List<Mutation> mutations, List<Integer> mutationLocations) {
        for (int i = 0; i < NUMBER_OF_MUTATIONS; i++) {
            Mutation mutation = mutations.get(i);
            int mutationLocation = mutationLocations.get(i);
            if (mutation == Mutation.TYPO) {
                String[] transactionArguments = receipt.get(mutationLocation).split(" ");
                int argumentNumber = RANDOM.nextInt(transactionArguments.length);
                String argument = transactionArguments[argumentNumber].toLowerCase();

                if (argument.matches("[a-zA-Z]*")) {

                } else if (argument.matches("[0-9]{8}")) {

                } else {

                }
            } else {
                swap(receipt, mutationLocation, mutationLocation + 1);
                transformation.set(mutationLocation + 1, String.format("This transaction is %s.", mutation.toString().toLowerCase()));
            }
        }
    }
}
