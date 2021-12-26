package server.game.pushing.paper.store;

import server.game.pushing.paper.store.bank.Bank;
import server.game.pushing.paper.store.handler.ChainOfResponsibilityFactory;
import server.game.pushing.paper.store.handler.Handler;
import server.game.pushing.paper.store.handler.TransactionType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static server.game.pushing.paper.store.handler.Handler.parseDouble;

public class Receipt {
    private final Bank BANK;
    private final Handler VALIDATOR;
    private final Handler PROCESSOR;

    private final Map<String, List<String>> TRANSACTIONS;

    public Receipt(Bank bank) {
        this.BANK = bank;
        ChainOfResponsibilityFactory chainOfResponsibilityFactory = new ChainOfResponsibilityFactory(BANK);
        VALIDATOR = chainOfResponsibilityFactory.getValidator();
        PROCESSOR = chainOfResponsibilityFactory.getProcessor();

        TRANSACTIONS = new HashMap<>();
        TRANSACTIONS.put(null, new ArrayList<>());
    }

    public List<String> output() {
        List<String> transactions = new ArrayList<>();

        for (String id : BANK.getAccounts()) {
            transactions.add(BANK.getAccount(id).toString());
            transactions.addAll(TRANSACTIONS.get(id));
            transactions.add("");
        }
        transactions.addAll(TRANSACTIONS.get(null));

        return transactions;
    }

    public void addTransaction(String transaction) {
        if (VALIDATOR.handleTransaction(transaction) && PROCESSOR.handleTransaction(transaction)) {
            addValidTransaction(transaction.toLowerCase().split(" "));
        } else {
            TRANSACTIONS.get(null).add("[invalid] " + transaction);
        }
    }

    private void addValidTransaction(String[] transactionArguments) {
        if (transactionArguments[0].equalsIgnoreCase(TransactionType.Create.name())) {
            TRANSACTIONS.put(transactionArguments[2], new ArrayList<>());
        } else if (transactionArguments[0].equalsIgnoreCase(TransactionType.Deposit.name()) || transactionArguments[0].equalsIgnoreCase(TransactionType.Withdraw.name())) {
            TRANSACTIONS.get(transactionArguments[1]).add(String.format("%s %s %.2f", transactionArguments[0], transactionArguments[1], parseDouble(transactionArguments[2])));
        } else if (transactionArguments[0].equalsIgnoreCase(TransactionType.Transfer.name())) {
            for (int i = 1; i < 3; i++) {
                TRANSACTIONS.get(transactionArguments[i]).add(String.format("%s %s %s %.2f", transactionArguments[0], transactionArguments[1], transactionArguments[2], parseDouble(transactionArguments[3])));
            }
        }
    }
}
