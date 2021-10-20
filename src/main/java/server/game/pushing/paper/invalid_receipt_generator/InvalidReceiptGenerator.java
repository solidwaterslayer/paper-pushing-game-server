package server.game.pushing.paper.invalid_receipt_generator;

import server.game.pushing.paper.store.Store;

import java.util.List;

public class InvalidReceiptGenerator {
    public List<String> getInvalidReceipt(List<String> order) {
        Store store = new Store();
        store.getOrder().addAll(order);
        return store.getReceipt();
        // typo
            // change transaction type
            // change account type
            // increment id
            // increment amount
        // move
        // switch
            // deposit
            // withdraw
            // transfer
        // remove
            // deposit
            // withdraw
            // transfer

        // overload
        // paradox
            // withdraw from savings twice
            // withdraw from cd before time traveling 12 months
    }

    public List<String> getReceiptTransformation() {
        return null;
    }
}
