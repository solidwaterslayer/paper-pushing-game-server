package server.game.pushing.paper.store;

import server.game.pushing.paper.store.bank.Bank;
import server.game.pushing.paper.store.chain_of_responsibility.ChainOfResponsibility;
import server.game.pushing.paper.store.chain_of_responsibility.ChainOfResponsibilityFactory;

import java.util.ArrayList;
import java.util.List;

public class Store {
    private final List<String> ORDER;
    private final Bank BANK;
    private final Receipt RECEIPT;
    private final ChainOfResponsibility VALIDATOR;
    private final ChainOfResponsibility PROCESSOR;

    public Store() {
        ORDER = new ArrayList<>();
        BANK = new Bank();
        RECEIPT = new Receipt(BANK);
        ChainOfResponsibilityFactory chainOfResponsibilityFactory = new ChainOfResponsibilityFactory(BANK);
        VALIDATOR = chainOfResponsibilityFactory.getChainOfResponsibility(true);
        PROCESSOR = chainOfResponsibilityFactory.getChainOfResponsibility(false);
    }

    public Bank getBank() {
        return BANK;
    }

    public List<String> getOrder() {
        return ORDER;
    }

    public void setOrder(List<String> order) {
        this.ORDER.addAll(order);
    }

    public List<String> getReceipt() {
        for (int i = RECEIPT.size(); i < ORDER.size(); i++) {
            String transaction = ORDER.get(i);
            RECEIPT.addTransaction(transaction, VALIDATOR.handle(transaction) && PROCESSOR.handle(transaction));
        }

        return this.RECEIPT.output();
    }
}
