package server.game.pushing.paper.bank;

import org.springframework.stereotype.Component;
import server.game.pushing.paper.bank.account.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class Bank {
    protected Map<String, Account> accounts;
    protected double minBalanceFee;

    public Bank(List<Account> accounts) {
        initializeBank();

        for (Account account : accounts) {
            this.accounts.put(account.getID(), account);
        }
    }

    public Bank() {
        initializeBank();
    }

    protected void initializeBank() {
        this.accounts = new HashMap<>();
        minBalanceFee = 25;
    }

    public void createChecking(String id, double apr) {
        accounts.put(id, new Checking(id, apr));
    }

    public void createSavings(String id, double apr) {
        accounts.put(id, new Savings(id, apr));
    }

    public void createCD(String id, double apr, double balance) {
        accounts.put(id, new CD(id, apr, balance));
    }

    public Map<String, Account> getAccounts() {
        return accounts;
    }

    public Account getAccount(String id) {
        return accounts.get(id);
    }

    public boolean containsAccount(String id) {
        return accounts.containsKey(id);
    }

    public double getMinBalanceFee() {
        return minBalanceFee;
    }

    public void deposit(String id, double depositAmount) {
        getAccount(id).deposit(depositAmount);
    }

    public void withdraw(String id, double withdrawAmount) {
        getAccount(id).withdraw(withdrawAmount);
    }

    public void transfer(String fromID, String toID, double transferAmount) {
        Account fromAccount = getAccount(fromID);

        if (transferAmount > fromAccount.getBalance()) {
            transfer(fromID, toID, fromAccount.getBalance());
            return;
        }

        fromAccount.withdraw(transferAmount);
        getAccount(toID).deposit(transferAmount);
    }

    public void passTime(int months) {
        for (int i = 0; i < months; i++) {
            for (String id : new ArrayList<>(accounts.keySet())) {
                Account account = getAccount(id);
                double accountBalance = account.getBalance();

                if (accountBalance == 0) {
                    accounts.remove(id);
                    continue;
                }
                if (accountBalance <= 100) {
                    account.withdraw(minBalanceFee);
                }

                account.applyAPR();
            }
        }
    }

    public boolean isIDValid(String id) {
        return !containsAccount(id) && id.matches("[0-9]{8}");
    }

    public boolean isAPRValid(double apr) {
        return 0 <= apr && apr <= 10;
    }

    public boolean isInitialCDBalanceValid(double balance) {
        return 1000 <= balance && balance <= 10000;
    }

    public boolean isDepositValid(String id, double depositAmount) {
        return containsAccount(id) && getAccount(id).isDepositValid(depositAmount);
    }

    public boolean isWithdrawValid(String id, double withdrawAmount) {
        return containsAccount(id) && getAccount(id).isWithdrawValid(withdrawAmount);
    }

    public boolean isTransferValid(String fromID, String toID, double transferAmount) {
        return !fromID.equals(toID) && isWithdrawValid(fromID, transferAmount) && isDepositValid(toID, transferAmount);
    }

    public boolean isPassTimeValid(int months) {
        return 1 <= months && months <= 60;
    }
}
