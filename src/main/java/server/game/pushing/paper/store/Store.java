package server.game.pushing.paper.store;

import server.game.pushing.paper.store.bank.Bank;
import server.game.pushing.paper.store.chainofresponsibility.ChainOfResponsibility;
import server.game.pushing.paper.store.chainofresponsibility.transactionprocessor.*;
import server.game.pushing.paper.store.chainofresponsibility.transactionvalidator.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Store {
    private Bank bank;
    private List<String> order;
    private Receipt receipt;
    private ChainOfResponsibility validator;
    private ChainOfResponsibility processor;

    public Store() {
        bank = new Bank();
        order = new ArrayList<>();
        receipt = new Receipt(bank);
        validator = ChainOfResponsibility.getInstance(Arrays.asList(
                new CreateValidator(bank),
                new DepositValidator(bank),
                new WithdrawValidator(bank),
                new TransferValidator(bank),
                new PassTimeValidator(bank), null)
        );
        processor = ChainOfResponsibility.getInstance(Arrays.asList(
                new CreateProcessor(bank),
                new DepositProcessor(bank),
                new WithdrawProcessor(bank),
                new TransferProcessor(bank),
                new PassTimeProcessor(bank),
                null
        ));
    }

    public Bank getBank() {
        return bank;
    }

    public List<String> getOrder() {
        return order;
    }

    public List<String> getReceipt() {
        for (String transaction : order) {
            receipt.addTransaction(transaction, validator.handle(transaction) && processor.handle(transaction));
        }

        return this.receipt.output();
    }
}
