package server.game.pushing.paper.store;

import server.game.pushing.paper.store.bank.Bank;
import server.game.pushing.paper.store.chainofresponsibility.TransactionType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Receipt {
    private final Bank BANK;
    private final Map<String, List<String>> TRANSACTIONS;

    public Receipt(Bank bank) {
        this.BANK = bank;
        TRANSACTIONS = new HashMap<>();
        TRANSACTIONS.put(null, new ArrayList<>());
    }

    public void addTransaction(String transaction, boolean isTransactionValid) {
        if (isTransactionValid) {
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

    public List<String> output() {
        List<String> transactions = new ArrayList<>();

        for (String id : BANK.getAccounts().keySet()) {
            transactions.add(BANK.getAccount(id).toString());
            transactions.addAll(this.TRANSACTIONS.get(id));
            transactions.add("");
        }
        transactions.addAll(this.TRANSACTIONS.get(null));

        return transactions;
    }
}
