package server.game.pushing.paper.store;

import server.game.pushing.paper.store.bank.Bank;
import server.game.pushing.paper.store.chain_of_responsibility.ChainOfResponsibility;
import server.game.pushing.paper.store.chain_of_responsibility.ChainOfResponsibilityFactory;
import server.game.pushing.paper.store.chain_of_responsibility.TransactionType;

import java.util.*;

public class Receipt {
    private final Bank BANK;
    private final ChainOfResponsibility VALIDATOR;
    private final ChainOfResponsibility PROCESSOR;

    private final Map<String, List<String>> TRANSACTIONS;

    public Receipt(Bank bank) {
        this.BANK = bank;
        ChainOfResponsibilityFactory chainOfResponsibilityFactory = new ChainOfResponsibilityFactory(BANK);
        VALIDATOR = chainOfResponsibilityFactory.getChainOfResponsibility(true);
        PROCESSOR = chainOfResponsibilityFactory.getChainOfResponsibility(false);

        TRANSACTIONS = new HashMap<>();
        TRANSACTIONS.put(null, new ArrayList<>());
    }

    public List<String> output() {
        List<String> transactions = new ArrayList<>();

        Iterator<String> iterator = BANK.getAccountIterator();
        while (iterator.hasNext()) {
            String id = iterator.next();

            transactions.add(BANK.getAccount(id).toString());
            transactions.addAll(TRANSACTIONS.get(id));
            transactions.add("");
        }
        transactions.addAll(TRANSACTIONS.get(null));

        return transactions;
    }

    public void addTransaction(String transaction) {
        if (VALIDATOR.handle(transaction) && PROCESSOR.handle(transaction)) {
            addValidTransaction(transaction.toLowerCase().split(" "));
        } else {
            TRANSACTIONS.get(null).add("[invalid] " + transaction);
        }
    }

    private void addValidTransaction(String[] transactionArguments) {
        if (transactionArguments[0].equalsIgnoreCase(TransactionType.Create.name())) {
            TRANSACTIONS.put(transactionArguments[2], new ArrayList<>());
        } else if (transactionArguments[0].equalsIgnoreCase(TransactionType.Deposit.name()) || transactionArguments[0].equalsIgnoreCase(TransactionType.Withdraw.name())) {
            TRANSACTIONS.get(transactionArguments[1]).add(transactionArguments[0] + " " + transactionArguments[1] + " " + transactionArguments[2]);
        } else if (transactionArguments[0].equalsIgnoreCase(TransactionType.Transfer.name())) {
            for (int i = 1; i < 3; i++) {
                TRANSACTIONS.get(transactionArguments[i]).add(transactionArguments[0] + " " + transactionArguments[1] + " " + transactionArguments[2] + " " + transactionArguments[3]);
            }
        }
    }
}
