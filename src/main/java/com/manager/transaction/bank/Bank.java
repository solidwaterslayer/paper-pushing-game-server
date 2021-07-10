package com.manager.transaction.bank;

import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class Bank {
    protected Map<String, Account> accounts;

    public Bank() {
        accounts = new HashMap<>();
    }

    public Bank(ArrayList<Account> accounts) {
        this.accounts = new HashMap<>();

        for (Account account : accounts) {
            this.accounts.put(account.getID(), account);
        }
    }

    public void putChecking(String id, double apr) {
        accounts.put(id, new Checking(id, apr));
    }

    public void putSavings(String id, double apr) {
        accounts.put(id, new Savings(id, apr));
    }

    public void putCD(String id, double apr, double balance) {
        accounts.put(id, new CD(id, apr, balance));
    }

    public boolean isInitialCDBalanceValid(double balance) {
        return 1000 <= balance && balance <= 10000;
    }

    public boolean isAPRValid(double apr) {
        return 0 <= apr && apr <= 10;
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

    public boolean isDepositValid(double depositAmount, String id) {
        return getAccount(id).isDepositValid(depositAmount);
    }

    public boolean isWithdrawValid(double withdrawAmount, String id) {
        return getAccount(id).isWithdrawValid(withdrawAmount);
    }

    public void passTime(int months) {
        for (int i = 0; i < months; i++) {
            for (String id : accounts.keySet()) {
                Account account = getAccount(id);
                double accountBalance = account.getBalance();

                if (accountBalance == 0) {
                    accounts.remove(id);
                    continue;
                }
                if (accountBalance < 100) {
                    account.applyMinBalanceFee();
                }

                account.applyAPR();
            }
        }
    }

    public boolean isPassTimeValid(int months) {
        return 1 <= months && months <= 60;
    }
}
