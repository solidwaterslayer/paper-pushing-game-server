package server.game.pushing.paper.ledgervalidator;

import server.game.pushing.paper.ledgervalidator.bank.Bank;

import java.util.*;

public class ValidLedger {
    Map<String, List<String>> transactions;
    protected Bank bank;

    public ValidLedger(Bank bank) {
        transactions = new HashMap<>();
        transactions.put(null, new ArrayList<>());
        this.bank = bank;
    }

    public void addTransaction(String transaction, boolean isTransactionValid) {
        addTransaction(isTransactionValid, transaction.toLowerCase());
    }

    protected void addTransaction(boolean isTransactionValid, String transaction) {
        if (isTransactionValid) {
            String[] transactionArguments = transaction.split(" ");

            switch (transactionArguments[0]) {
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
            transactions.get(null).add("invalid " + transaction);
        }
    }

    public List<String> getTransactions() {
        List<String> transactions = new ArrayList<>();

        for (String id : this.transactions.keySet()) {
            if (id == null || !bank.containsAccount(id)) {
                continue;
            }

            transactions.add(bank.getAccount(id).toString().toLowerCase());
            transactions.addAll(this.transactions.get(id));
        }
        transactions.addAll(this.transactions.get(null));

        return transactions;
    }
}
