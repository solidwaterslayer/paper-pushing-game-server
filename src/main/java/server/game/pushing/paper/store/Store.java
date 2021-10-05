package server.game.pushing.paper.store;

import server.game.pushing.paper.store.bank.Bank;

import java.util.ArrayList;
import java.util.List;

public class Store {
    private final List<String> ORDER;
    private final Bank BANK;
    private final Receipt RECEIPT;
    private int receiptInputSize;

    public Store() {
        ORDER = new ArrayList<>();
        BANK = new Bank();
        RECEIPT = new Receipt(BANK);
        receiptInputSize = 0;
    }

    public Bank getBank() {
        return BANK;
    }

    public List<String> getOrder() {
        return ORDER;
    }

    public List<String> getReceipt() {
        for (int i = receiptInputSize; i < ORDER.size(); i++) {
            RECEIPT.addTransaction(ORDER.get(i));

            receiptInputSize++;
        }

        return this.RECEIPT.output();
    }
}
