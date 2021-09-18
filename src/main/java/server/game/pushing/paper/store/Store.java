package server.game.pushing.paper.store;

import server.game.pushing.paper.store.bank.Bank;
import server.game.pushing.paper.store.chainofresponsibility.ChainOfResponsibility;
import server.game.pushing.paper.store.chainofresponsibility.transactionvalidator.*;

import java.util.Arrays;
import java.util.List;

public class Store {
    protected ChainOfResponsibility validator;
    protected ChainOfResponsibility processor;
    protected Bank bank;

    public Store(Bank bank) {
        validator = ChainOfResponsibility.getInstance(Arrays.asList(
                new CreateValidator(bank),
                new DepositValidator(bank),
                new WithdrawValidator(bank),
                new TransferValidator(bank),
                new PassTimeValidator(bank),
                null
        ));
        processor = ChainOfResponsibility.getInstance(Arrays.asList(
                new CreateValidator(bank),
                new DepositValidator(bank),
                new WithdrawValidator(bank),
                new TransferValidator(bank),
                new PassTimeValidator(bank),
                null
        ));
        this.bank = bank;
    }

    public List<String> validate(List<String> invalidLedger) {
        Order order = new Order(bank);

        for (String transaction : invalidLedger) {
            order.addTransaction(transaction, validator.handle(transaction) && processor.handle(transaction));
        }

        return order.getTransactions();
    }
}
