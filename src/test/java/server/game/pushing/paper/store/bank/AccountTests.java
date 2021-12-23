package server.game.pushing.paper.store.bank;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.game.pushing.paper.store.bank.account.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static server.game.pushing.paper.store.bank.Bank.getMonthsPerYear;
import static server.game.pushing.paper.store.bank.BankTests.timeTravel;

public class AccountTests {
    private Account checkingAccount;
    private Account savingsAccount;
    private Account cdAccount;

    private final String CHECKING_ID = "34785729";
    private final String SAVINGS_ID = "47012479";
    private final String CD_ID = "34782479";
    private final double STARTING_CD_BALANCE = 5835;

    private double checkingDepositAmount;
    private double checkingWithdrawAmount;
    private double savingsDepositAmount;
    private double savingsWithdrawAmount;
    private double cdWithdrawAmount;

    @BeforeEach
    protected void setUp() {
        checkingAccount = new CheckingAccount(CHECKING_ID);
        savingsAccount = new SavingsAccount(SAVINGS_ID);
        cdAccount = new CDAccount(CD_ID, STARTING_CD_BALANCE);
    }

    @Test
    protected void checking_accounts_should_start_with_0_balance() {
        Account account = checkingAccount;
        AccountType accountType = AccountType.CHECKING;
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
        AccountType accountType = AccountType.SAVINGS;
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
        AccountType accountType = AccountType.CD;
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

        assertEquals(1000, account.getMaxDepositAmount());
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

        assertEquals(2500, account.getMaxDepositAmount());
        assertTrue(account.isDepositAmountValid(1300));
        assertTrue(account.isDepositAmountValid(depositAmount - 1));
        assertTrue(account.isDepositAmountValid(depositAmount));
        assertFalse(account.isDepositAmountValid(depositAmount + 1));
        assertFalse(account.isDepositAmountValid(depositAmount + 1200));
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
    protected void checking_accounts_can_withdraw_when_the_withdraw_amount_is_less_than_the_account_balance() {
        checkingDepositAmount = checkingAccount.getMaxWithdrawAmount();
        checkingWithdrawAmount = checkingDepositAmount - 100;
        checkingAccount.deposit(checkingDepositAmount);

        assertTrue(checkingWithdrawAmount < checkingAccount.getBalance());
        checkingAccount.withdraw(checkingWithdrawAmount);
        assertEquals(checkingDepositAmount - checkingWithdrawAmount, checkingAccount.getBalance());
    }

    @Test
    protected void savings_accounts_can_withdraw_when_the_withdraw_amount_is_less_than_the_account_balance() {
        savingsDepositAmount = savingsAccount.getMaxWithdrawAmount();
        savingsWithdrawAmount = savingsDepositAmount - 50;
        savingsAccount.deposit(savingsDepositAmount);

        assertTrue(savingsWithdrawAmount < savingsAccount.getBalance());
        savingsAccount.withdraw(savingsWithdrawAmount);
        assertEquals(savingsDepositAmount - savingsWithdrawAmount, savingsAccount.getBalance());
    }

    @Test
    protected void accounts_can_withdraw_when_the_withdraw_amount_is_equal_to_the_account_balance() {
        int months = getMonthsPerYear();
        checkingDepositAmount = 300;
        savingsDepositAmount = 400;
        checkingWithdrawAmount = checkingDepositAmount;
        savingsWithdrawAmount = savingsDepositAmount;
        cdWithdrawAmount = timeTravel(STARTING_CD_BALANCE, new Bank(), months);

        cdAccount.timeTravel(months);
        transfer();

        assertEquals(0, checkingAccount.getBalance());
        assertEquals(0, savingsAccount.getBalance());
        assertEquals(0, cdAccount.getBalance());
    }

    @Test
    protected void accounts_should_withdraw_the_account_balance_when_the_withdraw_amount_is_greater_than_the_account_balance() {
        checkingDepositAmount = 300;
        savingsDepositAmount = 400;
        checkingWithdrawAmount = checkingAccount.getMaxWithdrawAmount();
        savingsWithdrawAmount = savingsAccount.getMaxWithdrawAmount();
        cdWithdrawAmount = cdAccount.getMaxWithdrawAmount();

        cdAccount.timeTravel(12);
        transfer();

        assertEquals(0, checkingAccount.getBalance());
        assertEquals(0, savingsAccount.getBalance());
        assertEquals(0, cdAccount.getBalance());
    }

    @Test
    protected void checking_accounts_should_withdraw_amounts_greater_than_0() {
        checkingWithdrawAmount = 0;

        assertFalse(checkingAccount.isWithdrawAmountValid(checkingWithdrawAmount - 500));
        assertFalse(checkingAccount.isWithdrawAmountValid(checkingWithdrawAmount - 50));
        assertFalse(checkingAccount.isWithdrawAmountValid(checkingWithdrawAmount));
        assertTrue(checkingAccount.isWithdrawAmountValid(checkingWithdrawAmount + 50));
        assertTrue(checkingAccount.isWithdrawAmountValid(100));
    }

    @Test
    protected void checking_accounts_should_withdraw_amounts_less_than_or_equal_to_400() {
        checkingWithdrawAmount = 400;

        assertEquals(400, checkingAccount.getMaxWithdrawAmount());
        assertTrue(checkingAccount.isWithdrawAmountValid(200));
        assertTrue(checkingAccount.isWithdrawAmountValid(checkingWithdrawAmount - 100));
        assertTrue(checkingAccount.isWithdrawAmountValid(checkingWithdrawAmount));
        assertFalse(checkingAccount.isWithdrawAmountValid(checkingWithdrawAmount + 100));
        assertFalse(checkingAccount.isWithdrawAmountValid(checkingWithdrawAmount + 500));
    }

    @Test
    protected void savings_accounts_should_withdraw_amounts_greater_than_0() {
        savingsWithdrawAmount = 0;

        assertFalse(savingsAccount.isWithdrawAmountValid(savingsWithdrawAmount - 1000));
        assertFalse(savingsAccount.isWithdrawAmountValid(savingsWithdrawAmount - 100));
        assertFalse(savingsAccount.isWithdrawAmountValid(savingsWithdrawAmount));
        assertTrue(savingsAccount.isWithdrawAmountValid(savingsWithdrawAmount + 100));
        assertTrue(savingsAccount.isWithdrawAmountValid(500));
    }

    @Test
    protected void savings_accounts_should_withdraw_amounts_less_than_or_equal_to_1000() {
        savingsWithdrawAmount = 1000;

        assertEquals(1000, savingsAccount.getMaxWithdrawAmount());
        assertTrue(savingsAccount.isWithdrawAmountValid(600));
        assertTrue(savingsAccount.isWithdrawAmountValid(savingsWithdrawAmount - 50));
        assertTrue(savingsAccount.isWithdrawAmountValid(savingsWithdrawAmount));
        assertFalse(savingsAccount.isWithdrawAmountValid(savingsWithdrawAmount + 50));
        assertFalse(savingsAccount.isWithdrawAmountValid(savingsWithdrawAmount + 1000));
    }

    @Test
    protected void cd_accounts_should_withdraw_amounts_greater_than_or_equal_to_the_account_balance() {
        int months = getMonthsPerYear();
        cdWithdrawAmount = timeTravel(STARTING_CD_BALANCE, new Bank(), months);

        cdAccount.timeTravel(months);

        assertEquals(cdWithdrawAmount, cdAccount.getBalance());
        assertFalse(cdAccount.isWithdrawAmountValid(cdWithdrawAmount - 500));
        assertFalse(cdAccount.isWithdrawAmountValid(cdWithdrawAmount - 100));
        assertTrue(cdAccount.isWithdrawAmountValid(cdWithdrawAmount));
        assertTrue(cdAccount.isWithdrawAmountValid(cdWithdrawAmount + 100));
        assertTrue(cdAccount.isWithdrawAmountValid(cdWithdrawAmount + 500));

        assertEquals(Double.POSITIVE_INFINITY, cdAccount.getMaxWithdrawAmount());
        assertFalse(cdAccount.isWithdrawAmountValid(-1000));
        assertFalse(cdAccount.isWithdrawAmountValid(0));
        assertTrue(cdAccount.isWithdrawAmountValid(Double.POSITIVE_INFINITY));
    }

    private void transfer() {
        checkingAccount.deposit(checkingDepositAmount);
        savingsAccount.deposit(savingsDepositAmount);
        checkingAccount.withdraw(checkingWithdrawAmount);
        savingsAccount.withdraw(savingsWithdrawAmount);
        cdAccount.withdraw(cdWithdrawAmount);
    }

    @Test
    protected void savings_accounts_can_withdraw_once_per_time_travel_event() {
        double withdrawAmount = savingsAccount.getMaxWithdrawAmount();

        assertTrue(savingsAccount.isWithdrawAmountValid(withdrawAmount));

        savingsAccount.withdraw(withdrawAmount);
        assertFalse(savingsAccount.isWithdrawAmountValid(withdrawAmount));

        savingsAccount.timeTravel(1);
        assertTrue(savingsAccount.isWithdrawAmountValid(withdrawAmount));
    }

    @Test
    protected void cd_accounts_can_withdraw_after_time_traveling_12_months() {
        int monthsPerYear = getMonthsPerYear();
        double withdrawAmount = cdAccount.getMaxWithdrawAmount();

        for (int month = 0; month < monthsPerYear * 2; month++) {
            assertEquals(cdAccount.getLifetime() >= getMonthsPerYear(), cdAccount.isWithdrawAmountValid(withdrawAmount));

            cdAccount.timeTravel(1);
        }
    }
}
