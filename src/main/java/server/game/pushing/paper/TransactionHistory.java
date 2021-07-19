package server.game.pushing.paper;

import server.game.pushing.paper.bank.Bank;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TransactionHistory {
    Map<String, List<String>> transactions;
    protected Bank bank;

    public TransactionHistory(Bank bank) {
        transactions = new HashMap<>();
        transactions.put(null, new ArrayList<>());
        this.bank = bank;
    }

    public void addTransaction(String transaction, boolean isTransactionValid) {
        if (isTransactionValid) {
            String[] transactionArguments = transaction.split(" ");

            switch (transactionArguments[0].toLowerCase()) {
                case "create":
                    transactions.put(transactionArguments[2], new ArrayList<>());
                    break;
                case "deposit":
                case "withdraw":
                    transactions.get(transactionArguments[1]).add(transaction);
                    break;
                default:
                    transactions.get(transactionArguments[1]).add(transaction);
                    transactions.get(transactionArguments[2]).add(transaction);
                    break;
            }
        } else {
            transactions.get(null).add(transaction);
        }
    }

    public List<String> getTransactions() {
        List<String> transactions = new ArrayList<>();

        for (String id : this.transactions.keySet()) {
            if (id == null || !bank.containsAccount(id)) {
                continue;
            }

            transactions.add(bank.getAccount(id).toString());
            transactions.addAll(this.transactions.get(id));
        }
        transactions.addAll(this.transactions.get(null));

        return transactions;
    }
}
