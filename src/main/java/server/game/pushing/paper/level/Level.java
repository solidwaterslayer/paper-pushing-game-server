package server.game.pushing.paper.level;

import server.game.pushing.paper.generator.OrderGenerator;
import server.game.pushing.paper.store.Store;
import server.game.pushing.paper.store.bank.AccountType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static java.lang.Double.parseDouble;
import static java.lang.Integer.parseInt;
import static java.lang.String.join;
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
        List<String> transactionArguments = new ArrayList<>(Arrays.asList(receipt.get(location).split(" ")));
        int microLocation = random.nextInt(transactionArguments.size());
        String transactionArgument = transactionArguments.get(microLocation);

        String microMutation = transactionArgument;
        if (transactionArgument.matches("[a-zA-Z]*")) {
            while (microMutation.equals(transactionArgument)) {
                microMutation = AccountType.values()[random.nextInt(AccountType.values().length)].name().toLowerCase();
            }
        } else if (transactionArgument.matches("[0-9]{8}")) {
            microMutation = String.format("0000000%s", parseInt(transactionArgument) + 1);
            microMutation = microMutation.substring(microMutation.length() - 8);
        } else {
            microMutation = String.format("%.2f", parseDouble(transactionArgument) + 100);
        }

        transactionArguments.set(microLocation, microMutation);
        receipt.set(location, join(" ", transactionArguments));
        transformation.set(location, Typo.name());
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
