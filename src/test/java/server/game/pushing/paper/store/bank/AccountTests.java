package server.game.pushing.paper.store.bank;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.game.pushing.paper.store.bank.account.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.Math.max;
import static org.junit.jupiter.api.Assertions.*;
import static server.game.pushing.paper.store.bank.Bank.getMonthsPerYear;
import static server.game.pushing.paper.store.bank.account.AccountType.*;

public class AccountTests {
    private Account checkingAccount;
    private Account savingsAccount;
    private Account cdAccount;

    private final String CHECKING_ID = "34785729";
    private final String SAVINGS_ID = "47012479";
    private final String CD_ID = "34782479";
    private final double STARTING_CD_BALANCE = 5835;

    @BeforeEach
    protected void setUp() {
        checkingAccount = new CheckingAccount(CHECKING_ID);
        savingsAccount = new SavingsAccount(SAVINGS_ID);
        cdAccount = new CDAccount(CD_ID, STARTING_CD_BALANCE);
    }

    @Test
    protected void checking_accounts_should_start_with_0_balance() {
        Account account = checkingAccount;
        AccountType accountType = CHECKING;
        String id = CHECKING_ID;
        double balance = 0;

        assertEquals(accountType, account.getAccountType());
        assertEquals(id, account.getID());
        assertEquals(balance, account.getBalance());
        assertEquals(String.format("%s %s %.2f", accountType, id, balance).toLowerCase(), account.toString());
    }

    @Test
    protected void savings_accounts_should_start_with_0_balance() {
        Account account = savingsAccount;
        AccountType accountType = SAVINGS;
        String id = SAVINGS_ID;
        double balance = 0;

        assertEquals(accountType, account.getAccountType());
        assertEquals(id, account.getID());
        assertEquals(balance, account.getBalance());
        assertEquals(String.format("%s %s %.2f", accountType, id, balance).toLowerCase(), account.toString());
    }

    @Test
    protected void cd_accounts_can_be_created() {
        Account account = cdAccount;
        AccountType accountType = CD;
        String id = CD_ID;
        double balance = STARTING_CD_BALANCE;

        assertEquals(accountType, account.getAccountType());
        assertEquals(id, account.getID());
        assertEquals(balance, account.getBalance());
        assertEquals(String.format("%s %s %.2f", accountType, id, balance).toLowerCase(), account.toString());
    }

    @Test
    protected void checking_accounts_can_deposit() {
        Account account = checkingAccount;
        double depositAmount = account.getMaxDepositAmount();

        account.deposit(depositAmount);

        assertEquals(depositAmount, account.getBalance());
    }

    @Test
    protected void savings_accounts_can_deposit() {
        Account account = savingsAccount;
        double depositAmount = account.getMaxDepositAmount();

        account.deposit(depositAmount);

        assertEquals(depositAmount, account.getBalance());
    }

    @Test
    protected void cd_accounts_can_not_deposit() {
        Account account = cdAccount;
        List<Double> depositAmounts = new ArrayList<>(Arrays.asList(-500., -1., 0., 1., 500., 600., 999., 1000., 1001., 1500.));

        assertEquals(0, account.getMaxDepositAmount());
        for (double depositAmount : depositAmounts) {
            assertFalse(account.isDepositAmountValid(depositAmount));
        }
    }

    @Test
    protected void checking_accounts_should_deposit_amounts_greater_than_0() {
        Account account = checkingAccount;
        double depositAmount = 0;

        assertFalse(account.isDepositAmountValid(depositAmount - 500));
        assertFalse(account.isDepositAmountValid(depositAmount - 1));
        assertFalse(account.isDepositAmountValid(depositAmount));
        assertTrue(account.isDepositAmountValid(depositAmount + 1));
        assertTrue(account.isDepositAmountValid(500));
    }

    @Test
    protected void checking_accounts_should_deposit_amounts_less_than_or_equal_to_1000() {
        Account account = checkingAccount;
        double depositAmount = account.getMaxDepositAmount();

        assertEquals(1000, depositAmount);
        assertTrue(account.isDepositAmountValid(600));
        assertTrue(account.isDepositAmountValid(depositAmount - 1));
        assertTrue(account.isDepositAmountValid(depositAmount));
        assertFalse(account.isDepositAmountValid(depositAmount + 1));
        assertFalse(account.isDepositAmountValid(depositAmount + 500));
    }

    @Test
    protected void savings_accounts_should_deposit_amounts_greater_than_0() {
        Account account = savingsAccount;
        double depositAmount = 0;

        assertFalse(account.isDepositAmountValid(depositAmount - 1200));
        assertFalse(account.isDepositAmountValid(depositAmount - 1));
        assertFalse(account.isDepositAmountValid(depositAmount));
        assertTrue(account.isDepositAmountValid(depositAmount + 1));
        assertTrue(account.isDepositAmountValid(1200));
    }

    @Test
    protected void savings_accounts_should_deposit_amounts_less_than_or_equal_to_2500() {
        Account account = savingsAccount;
        double depositAmount = account.getMaxDepositAmount();

        assertEquals(2500, depositAmount);
        assertTrue(account.isDepositAmountValid(1300));
        assertTrue(account.isDepositAmountValid(depositAmount - 1));
        assertTrue(account.isDepositAmountValid(depositAmount));
        assertFalse(account.isDepositAmountValid(depositAmount + 1));
        assertFalse(account.isDepositAmountValid(depositAmount + 1200));
    }

    @Test
    protected void checking_and_savings_accounts_can_withdraw_when_the_withdraw_amount_is_less_than_the_account_balance() {
        List<Account> accounts = new ArrayList<>(Arrays.asList(checkingAccount, savingsAccount));
        for (Account account : accounts) {
            double depositAmount = account.getMaxWithdrawAmount();
            double withdrawAmount = depositAmount / 2;

            account.deposit(depositAmount);
            assertTrue(withdrawAmount < account.getBalance());

            account.withdraw(withdrawAmount);
            assertEquals(depositAmount - withdrawAmount, account.getBalance());
        }
    }

    @Test
    protected void accounts_can_withdraw_when_the_withdraw_amount_is_equal_to_the_account_balance() {
        int months = getMonthsPerYear();
        List<Account> accounts = new ArrayList<>(Arrays.asList(checkingAccount, savingsAccount, cdAccount));
        for (Account account : accounts) {
            double transferAmount = max(account.getMaxWithdrawAmount(), account.getBalance());

            if (account.getAccountType() != CD) {
                account.deposit(transferAmount);
            } else {
                account.timeTravel(months);
            }
            account.withdraw(transferAmount);

            assertEquals(0, account.getBalance());
        }
    }

    @Test
    protected void accounts_should_withdraw_the_account_balance_when_the_withdraw_amount_is_greater_than_the_account_balance() {
        int months = getMonthsPerYear();
        List<Account> accounts = new ArrayList<>(Arrays.asList(checkingAccount, savingsAccount, cdAccount));
        for (Account account : accounts) {
            double withdrawAmount = account.getMaxWithdrawAmount();
            double depositAmount = withdrawAmount / 2;

            if (account.getAccountType() != CD) {
                account.deposit(depositAmount);
            } else {
                account.timeTravel(months);
            }
            assertTrue(withdrawAmount > account.getBalance());

            account.withdraw(withdrawAmount);
            assertEquals(0, account.getBalance());
        }
    }

    @Test
    protected void checking_accounts_should_withdraw_amounts_greater_than_0() {
        Account account = checkingAccount;
        double withdrawAmount = 0;

        assertFalse(account.isWithdrawAmountValid(withdrawAmount - 100));
        assertFalse(account.isWithdrawAmountValid(withdrawAmount - 1));
        assertFalse(account.isWithdrawAmountValid(withdrawAmount));
        assertTrue(account.isWithdrawAmountValid(withdrawAmount + 1));
        assertTrue(account.isWithdrawAmountValid(100));
    }

    @Test
    protected void checking_accounts_should_withdraw_amounts_less_than_or_equal_to_400() {
        Account account = checkingAccount;
        double withdrawAmount = account.getMaxWithdrawAmount();

        assertEquals(400, withdrawAmount);
        assertTrue(account.isWithdrawAmountValid(200));
        assertTrue(account.isWithdrawAmountValid(withdrawAmount - 1));
        assertTrue(account.isWithdrawAmountValid(withdrawAmount));
        assertFalse(account.isWithdrawAmountValid(withdrawAmount + 1));
        assertFalse(account.isWithdrawAmountValid(withdrawAmount + 100));
    }

    @Test
    protected void savings_accounts_should_withdraw_amounts_greater_than_0() {
        Account account = savingsAccount;
        double withdrawAmount = 0;

        assertFalse(account.isWithdrawAmountValid(withdrawAmount - 500));
        assertFalse(account.isWithdrawAmountValid(withdrawAmount - 1));
        assertFalse(account.isWithdrawAmountValid(withdrawAmount));
        assertTrue(account.isWithdrawAmountValid(withdrawAmount + 1));
        assertTrue(account.isWithdrawAmountValid(500));
    }

    @Test
    protected void savings_accounts_should_withdraw_amounts_less_than_or_equal_to_1000() {
        Account account = savingsAccount;
        double withdrawAmount = account.getMaxWithdrawAmount();

        assertEquals(1000, withdrawAmount);
        assertTrue(account.isWithdrawAmountValid(600));
        assertTrue(account.isWithdrawAmountValid(withdrawAmount - 1));
        assertTrue(account.isWithdrawAmountValid(withdrawAmount));
        assertFalse(account.isWithdrawAmountValid(withdrawAmount + 1));
        assertFalse(account.isWithdrawAmountValid(withdrawAmount + 500));
    }

    @Test
    protected void cd_accounts_should_withdraw_amounts_greater_than_or_equal_to_the_account_balance() {
        int months = getMonthsPerYear();
        Account account = cdAccount;
        double withdrawAmount = STARTING_CD_BALANCE;

        account.timeTravel(months);

        assertEquals(account.getBalance(), withdrawAmount);
        assertFalse(account.isWithdrawAmountValid(withdrawAmount - 500));
        assertFalse(account.isWithdrawAmountValid(withdrawAmount - 1));
        assertTrue(account.isWithdrawAmountValid(withdrawAmount));
        assertTrue(account.isWithdrawAmountValid(withdrawAmount + 1));
        assertTrue(account.isWithdrawAmountValid(withdrawAmount + 500));

        withdrawAmount = account.getMaxWithdrawAmount();
        assertEquals(Double.POSITIVE_INFINITY, withdrawAmount);
        assertTrue(account.isWithdrawAmountValid(withdrawAmount));
    }

    @Test
    protected void savings_accounts_can_withdraw_once_per_time_travel_event() {
        int months = 1;
        Account account = savingsAccount;
        double withdrawAmount = account.getMaxWithdrawAmount();

        account.withdraw(withdrawAmount);
        assertFalse(account.isWithdrawAmountValid(withdrawAmount));

        account.timeTravel(months);
        assertTrue(account.isWithdrawAmountValid(withdrawAmount));
    }

    @Test
    protected void cd_accounts_can_withdraw_after_time_traveling_12_months() {
        Account account = cdAccount;
        double withdrawAmount = account.getMaxWithdrawAmount();

        for (int months = 0; months < 24; months++) {
            assertEquals(months >= getMonthsPerYear(), account.isWithdrawAmountValid(withdrawAmount));
            account.timeTravel(1);
        }
    }
}
