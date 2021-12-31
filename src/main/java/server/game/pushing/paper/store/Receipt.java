package server.game.pushing.paper.store;

import server.game.pushing.paper.store.bank.Bank;
import server.game.pushing.paper.store.handler.ChainOfResponsibility;
import server.game.pushing.paper.store.handler.Handler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static server.game.pushing.paper.store.handler.Handler.parseDouble;
import static server.game.pushing.paper.store.handler.TransactionType.*;

public class Receipt {
    private final Map<String, List<String>> transactions;

    private final Bank bank;
    private final Handler validator;
    private final Handler processor;

    public Receipt() {
        transactions = new HashMap<>();
        transactions.put(null, new ArrayList<>());

        bank = new Bank();
        ChainOfResponsibility chainOfResponsibility = new ChainOfResponsibility(bank);
        validator = chainOfResponsibility.getValidator();
        processor = chainOfResponsibility.getProcessor();
    }

    public List<String> output() {
        List<String> transactions = new ArrayList<>();

        for (String id : bank.getAccounts()) {
            transactions.add(bank.getAccount(id).toString());
            transactions.addAll(this.transactions.get(id));
            transactions.add("");
        }
        transactions.addAll(this.transactions.get(null));

        return transactions;
    }

    public void addTransaction(String transaction) {
        if (validator.handleTransaction(transaction) && processor.handleTransaction(transaction)) {
            addValidTransaction(transaction.toLowerCase().split(" "));
        } else {
            transactions.get(null).add("[invalid] " + transaction);
        }
    }

    private void addValidTransaction(String[] transactionArguments) {
        if (transactionArguments[0].equalsIgnoreCase(Create.name())) {
            transactions.put(transactionArguments[2], new ArrayList<>());
        } else if (transactionArguments[0].equalsIgnoreCase(Deposit.name())
                || transactionArguments[0].equalsIgnoreCase(Withdraw.name())) {
            transactions.get(transactionArguments[1]).add(String.format("%s %s %.2f", transactionArguments[0], transactionArguments[1], parseDouble(transactionArguments[2])));
        } else if (transactionArguments[0].equalsIgnoreCase(Transfer.name())) {
            transactions.get(transactionArguments[1]).add(String.format("%s %s %s %.2f", transactionArguments[0], transactionArguments[1], transactionArguments[2], parseDouble(transactionArguments[3])));
            transactions.get(transactionArguments[2]).add(String.format("%s %s %s %.2f", transactionArguments[0], transactionArguments[1], transactionArguments[2], parseDouble(transactionArguments[3])));
        }
    }
}
