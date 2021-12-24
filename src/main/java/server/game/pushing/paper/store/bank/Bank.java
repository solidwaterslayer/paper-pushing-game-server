package server.game.pushing.paper.store.bank;

import server.game.pushing.paper.store.bank.account.Account;
import server.game.pushing.paper.store.bank.account.CDAccount;
import server.game.pushing.paper.store.bank.account.CheckingAccount;
import server.game.pushing.paper.store.bank.account.SavingsAccount;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Math.min;

public class Bank {
    private final Map<String, Account> accounts;

    private final double minCDBalance;
    private final double maxCDBalance;
    private final int maxTimeTravel;

    public Bank() {
        accounts = new LinkedHashMap<>();

        minCDBalance = 1000;
        maxCDBalance = 10000;
        maxTimeTravel = 60;
    }

    public void createCheckingAccount(String id) {
        accounts.put(id, new CheckingAccount(id));
    }

    public void createSavingsAccount(String id) {
        accounts.put(id, new SavingsAccount(id));
    }

    public void createCDAccount(String id, double balance) {
        accounts.put(id, new CDAccount(id, balance));
    }

    public List<String> getAccounts() {
        return new ArrayList<>(accounts.keySet());
    }

    public Account getAccount(String id) {
        return accounts.get(id);
    }

    public void removeAccount(String id) {
        accounts.remove(id);
    }

    public boolean isEmpty() {
        return accounts.isEmpty();
    }

    public int size() {
        return accounts.size();
    }

    public boolean containsAccount(String id) {
        return accounts.containsKey(id);
    }

    public double getMinBalanceFee() {
        return 100;
    }

    public boolean isLowBalanceAccount(Account account) {
        return account.getBalance() <= 900;
    }

    public void timeTravel(int months) {
        for (Account account : new ArrayList<>(accounts.values())) {
            if (isLowBalanceAccount(account)) {
                account.withdraw(getMinBalanceFee() * months);
            }

            account.timeTravel(months);
        }
    }

    public void deposit(String id, double depositAmount) {
        getAccount(id).deposit(depositAmount);
    }

    public void withdraw(String id, double withdrawAmount) {
        getAccount(id).withdraw(withdrawAmount);
    }

    public void transfer(String payingID, String receivingID, double transferAmount) {
        Account payingAccount = getAccount(payingID);
        Account receivingAccount = getAccount(receivingID);
        transferAmount = min(transferAmount, payingAccount.getBalance());

        payingAccount.withdraw(transferAmount);
        receivingAccount.deposit(transferAmount);
    }

    public boolean isIDValid(String id) {
        return !containsAccount(id) && id.matches("[0-9]{8}");
    }

    public boolean isCDBalanceValid(double cdBalance) {
        return minCDBalance <= cdBalance && cdBalance <= maxCDBalance;
    }

    public boolean isTimeTravelValid(int months) {
        return 0 < months && months <= maxTimeTravel;
    }

    public boolean isDepositAmountValid(String id, double depositAmount) {
        return containsAccount(id) && getAccount(id).isDepositAmountValid(depositAmount);
    }

    public boolean isWithdrawAmountValid(String id, double withdrawAmount) {
        return containsAccount(id) && getAccount(id).isWithdrawAmountValid(withdrawAmount);
    }

    public boolean isTransferAmountValid(String fromID, String toID, double transferAmount) {
        return !fromID.equals(toID) && isWithdrawAmountValid(fromID, transferAmount) && isDepositAmountValid(toID, transferAmount);
    }

    public double getMinCDBalance() {
        return minCDBalance;
    }

    public double getMaxCDBalance() {
        return maxCDBalance;
    }

    public static int getMonthsPerYear() {
        return 12;
    }

    public int getMaxTimeTravel() {
        return maxTimeTravel;
    }
}
