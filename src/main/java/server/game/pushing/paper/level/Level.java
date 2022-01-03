package server.game.pushing.paper.level;

import server.game.pushing.paper.generator.OrderGenerator;
import server.game.pushing.paper.store.Store;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static java.lang.String.valueOf;
import static java.util.Collections.swap;
import static server.game.pushing.paper.level.Mutation.Move;
import static server.game.pushing.paper.level.Mutation.Typo;

public class Level {
    private Random random;
    public List<String> order;
    public List<String> receipt;
    public List<String> transformation;

    private int size;
    private List<Mutation> mutations;
    private List<Integer> locations;

    public Level() {
        initializeTransformation();
        generateMutations();
        generateLocations();
        placeMutations();
    }

    private void initializeTransformation() {
        random = new Random();
        order = (new OrderGenerator()).generateOrder(random, 6);
        receipt = (new Store() {{ setOrder(order); }}).getReceipt();
        transformation = new ArrayList<>();
        for (int i = 0; i < receipt.size(); i++) {
            transformation.add(valueOf(i));
        }
    }

    private void generateMutations() {
        size = 2;
        List<Mutation> potentialMutations = new ArrayList<>(Arrays.asList(Typo, Typo, Move));
        mutations = new ArrayList<>();
        while (mutations.size() < size) {
            int mutation = random.nextInt((potentialMutations.size()));

            mutations.add(potentialMutations.get(mutation));
            potentialMutations.remove(mutation);
        }
    }

    private void generateLocations() {
        locations = new ArrayList<>();
        while (locations.size() < size) {
            int location = random.nextInt(receipt.size());

            if (!receipt.get(location).equals("") && !locations.contains(location)) {
                locations.add(location);
            }
        }
    }

    private void placeTypo(int location) {
        String[] transactionArguments = receipt.get(location).split(" ");
        location = random.nextInt(transactionArguments.length);
        String transactionArgument = transactionArguments[location];

        if (transactionArgument.matches("[a-zA-Z]*")) {
        } else if (transactionArgument.matches("[0-9]{8}")) {
        } else {
        }
    }

    private void placeMove(int location) {
        swap(receipt, location, location + 1);
        transformation.set(location + 1, Move.name());
    }

    private void placeMutations() {
        for (int i = 0; i < size; i++) {
            Mutation mutation = mutations.get(i);
            int location = locations.get(i);

            if (mutation == Typo) {
                placeTypo(location);
            } else {
                placeMove(location);
            }
        }
    }
}
