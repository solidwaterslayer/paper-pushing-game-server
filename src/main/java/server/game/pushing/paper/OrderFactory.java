package server.game.pushing.paper;

import server.game.pushing.paper.store.bank.Bank;
import server.game.pushing.paper.store.bank.account.Account;
import server.game.pushing.paper.store.bank.account.AccountType;
import server.game.pushing.paper.store.chainofresponsibility.TransactionType;

import java.util.*;

public class OrderFactory {
    private List<String> order;
    private Bank bank;

    private Random random;

    public List<String> getOrder(double seed) {
        order = new ArrayList<>();
        bank = new Bank();

        List<TransactionType> transactionTypes;
        random = new Random((long) seed);

        for (int i = 0; i < random.nextInt(5) + 4; i++) {
            transactionTypes = new ArrayList<>();
            transactionTypes.add(TransactionType.Create);
            transactionTypes.add(TransactionType.PassTime);

            if (bank.size() > 0) {
                transactionTypes.add(TransactionType.Deposit);
                transactionTypes.add(TransactionType.Withdraw);
            }
            if (bank.size() > 1) {
                transactionTypes.add(TransactionType.Transfer);
            }

            TransactionType transactionType = transactionTypes.get(random.nextInt(transactionTypes.size()));
            switch (transactionType) {
                case Create:
                    addCreateOrder(transactionType);
                    break;
                case Deposit:
                    addDepositOrder(transactionType);
                    break;
                case Withdraw:
                    addWithdrawOrder(transactionType);
                    break;
                case Transfer:
                    addTransferOrder(transactionType);
                    break;
                default:
                    addPassTimeOrder(transactionType);
                    break;
            }
        }

        return order;
    }

    private void addCreateOrder(TransactionType transactionType) {
        String id = random.ints(48, 58).limit(10).collect(
                StringBuilder :: new,
                StringBuilder :: appendCodePoint,
                StringBuilder :: append
        ).toString();
        double apr = bank.getMaxAPR() * random.nextDouble();
        double initialCDBalance = (bank.getMaxInitialCDBalance() - bank.getMinInitialCDBalance()) * random.nextDouble() + bank.getMinInitialCDBalance();

        if (!bank.isIDValid(id) || !bank.isAPRValid(apr) || !bank.isInitialCDBalanceValid(initialCDBalance)) {
            System.out.printf("id: %s\napr: %s\ninitial cd balance: %s\n", id, apr, initialCDBalance);
        }

        AccountType accountType = AccountType.values()[random.nextInt(AccountType.values().length)];
        switch (accountType) {
            case Checking:
                bank.createChecking(id, apr);
                break;
            case Savings:
                bank.createSavings(id, apr);
                break;
            default:
                order.add(String.format("%s %s %s %s %s", transactionType, accountType, id, apr, initialCDBalance));
                bank.createCD(id, apr, initialCDBalance);
                return;
        }

        order.add(String.format("%s %s %s %s", transactionType, accountType, id, apr));
    }

    private void addDepositOrder(TransactionType transactionType) {

    }

    private void addWithdrawOrder(TransactionType transactionType) {

    }

    private void addTransferOrder(TransactionType transactionType) {

    }

    private void addPassTimeOrder(TransactionType transactionType) {

    }
}
