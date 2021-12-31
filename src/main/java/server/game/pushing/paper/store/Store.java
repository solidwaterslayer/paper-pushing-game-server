package server.game.pushing.paper.store;

import java.util.List;

public class Store {
    private List<String> order;

    public void setOrder(List<String> order) {
        this.order = order;
    }

    public List<String> getReceipt() {
        Receipt receipt = new Receipt();

        for (String transaction : order) {
            receipt.addTransaction(transaction);
        }

        return receipt.output();
    }
}
