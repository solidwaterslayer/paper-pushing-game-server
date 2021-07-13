package com.manager.transaction.bank;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class BankTests {
    protected Bank bank;

    protected final String CHECKING_ID_0 = "00000000";
    protected final String CHECKING_ID_1 = "10000000";
    protected final String SAVINGS_ID_0 = "00000001";
    protected final String SAVINGS_ID_1 = "10000001";
    protected final String CD_ID = "00000010";
    protected final double APR = 0.1;
    protected final double INITIAL_BALANCE = 0;
    protected final double INITIAL_CD_BALANCE = 1000;

    protected double checkingDepositAmount;
    protected double checkingWithdrawAmount;
    protected double savingsDepositAmount;
    protected double savingsWithdrawAmount;
    protected double cdWithdrawAmount;
    protected double transferAmount;

    @BeforeEach
    protected void setUp() {
        bank = new Bank(new ArrayList<>(Arrays.asList(
                new Checking(CHECKING_ID_1, APR),
                new Savings(SAVINGS_ID_1, APR)
        )));
        bank.createChecking(CHECKING_ID_0, APR);
        bank.createSavings(SAVINGS_ID_0, APR);
        bank.createCD(CD_ID, APR, INITIAL_CD_BALANCE);
    }

    @Test
    protected void initialize_bank_should_have_0_accounts() {
        assertEquals(0, (new Bank()).getAccounts().size());
    }

    @Test
    protected void create_checking_should_be_possible() {
        Account checkingAccount0 = bank.getAccount(CHECKING_ID_0);

        assertEquals("Checking", checkingAccount0.getClass().getSimpleName());
        assertEquals(CHECKING_ID_0, checkingAccount0.getID());
        assertEquals(APR, checkingAccount0.getAPR());
        assertEquals(INITIAL_BALANCE, checkingAccount0.getBalance());
    }

    @Test
    protected void create_savings_should_be_possible() {
        Account savingsAccount0 = bank.getAccount(SAVINGS_ID_0);

        assertEquals("Savings", savingsAccount0.getClass().getSimpleName());
        assertEquals(SAVINGS_ID_0, savingsAccount0.getID());
        assertEquals(APR, savingsAccount0.getAPR());
        assertEquals(INITIAL_BALANCE, savingsAccount0.getBalance());
    }

    @Test
    protected void create_cd_should_be_possible() {
        Account cdAccount = bank.getAccount(CD_ID);

        assertEquals("CD", cdAccount.getClass().getSimpleName());
        assertEquals(CD_ID, cdAccount.getID());
        assertEquals(APR, cdAccount.getAPR());
        assertEquals(INITIAL_CD_BALANCE, cdAccount.getBalance());
    }

    @Test
    protected void deposit_checking_should_be_possible() {
        checkingDepositAmount = 400;

        bank.deposit(CHECKING_ID_0, checkingDepositAmount);

        assertEquals(checkingDepositAmount, bank.getAccount(CHECKING_ID_0).getBalance());
    }

    @Test
    protected void deposit_savings_should_be_possible() {
        savingsDepositAmount = 300;

        bank.deposit(SAVINGS_ID_0, savingsDepositAmount);

        assertEquals(savingsDepositAmount, bank.getAccount(SAVINGS_ID_0).getBalance());
    }

    @Test
    protected void withdraw_should_be_possible() {
        checkingDepositAmount = 500;
        checkingWithdrawAmount = 300;
        savingsDepositAmount = 400;
        savingsWithdrawAmount = 400;
        cdWithdrawAmount = 2000;

        bank.deposit(CHECKING_ID_0, checkingDepositAmount);
        bank.withdraw(CHECKING_ID_0, checkingWithdrawAmount);
        bank.deposit(SAVINGS_ID_0, savingsDepositAmount);
        bank.withdraw(SAVINGS_ID_0, savingsWithdrawAmount);
        bank.withdraw(CD_ID, cdWithdrawAmount);

        assertEquals(checkingDepositAmount - checkingWithdrawAmount, bank.getAccount(CHECKING_ID_0).getBalance());
        assertEquals(0, bank.getAccount(SAVINGS_ID_0).getBalance());
        assertEquals(0, bank.getAccount(CD_ID).getBalance());
    }

    @Test
    protected void transfer_when_equal_to_balance_should_be_possible() {
        checkingDepositAmount = 500;

        bank.deposit(CHECKING_ID_1, checkingDepositAmount);
        bank.transfer(CHECKING_ID_1, SAVINGS_ID_0, checkingDepositAmount);

        assertEquals(0, bank.getAccount(CHECKING_ID_1).getBalance());
        assertEquals(checkingDepositAmount, bank.getAccount(SAVINGS_ID_0).getBalance());
    }

    @Test
    protected void transfer_when_greater_than_balance_should_transfer_balance() {
        savingsDepositAmount = 100;
        transferAmount = 1000;

        bank.deposit(SAVINGS_ID_0, savingsDepositAmount);
        bank.transfer(SAVINGS_ID_0, SAVINGS_ID_1, transferAmount);

        assertEquals(0, bank.getAccount(SAVINGS_ID_0).getBalance());
        assertEquals(savingsDepositAmount, bank.getAccount(SAVINGS_ID_1).getBalance());
    }

    @Test
    protected void transfer_from_checking_to_checking_should_be_possible() {
        checkingDepositAmount = 500;
        transferAmount = 200;

        bank.deposit(CHECKING_ID_0, checkingDepositAmount);
        bank.transfer(CHECKING_ID_0, CHECKING_ID_1, transferAmount);

        assertEquals(checkingDepositAmount - transferAmount, bank.getAccount(CHECKING_ID_0).getBalance());
        assertEquals(transferAmount, bank.getAccount(CHECKING_ID_1).getBalance());
    }

    @Test
    protected void transfer_from_checking_to_savings_should_be_possible() {
        checkingDepositAmount = 1000;
        transferAmount = 400;

        bank.deposit(CHECKING_ID_1, checkingDepositAmount);
        bank.transfer(CHECKING_ID_1, SAVINGS_ID_1, transferAmount);

        assertEquals(checkingDepositAmount - transferAmount, bank.getAccount(CHECKING_ID_1).getBalance());
        assertEquals(transferAmount, bank.getAccount(SAVINGS_ID_1).getBalance());
    }

    @Test
    protected void transfer_from_savings_to_checking_should_be_possible() {
        savingsDepositAmount = 1500;
        transferAmount = 500;

        bank.deposit(SAVINGS_ID_0, savingsDepositAmount);
        bank.transfer(SAVINGS_ID_0, CHECKING_ID_0, transferAmount);

        assertEquals(savingsDepositAmount - transferAmount, bank.getAccount(SAVINGS_ID_0).getBalance());
        assertEquals(transferAmount, bank.getAccount(CHECKING_ID_0).getBalance());
    }

    @Test
    protected void transfer_from_savings_to_savings_should_be_possible() {
        savingsDepositAmount = 2000;
        transferAmount = 1000;

        bank.deposit(SAVINGS_ID_1, savingsDepositAmount);
        bank.transfer(SAVINGS_ID_1, SAVINGS_ID_0, transferAmount);

        assertEquals(savingsDepositAmount - transferAmount, bank.getAccount(SAVINGS_ID_1).getBalance());
        assertEquals(transferAmount, bank.getAccount(SAVINGS_ID_0).getBalance());
    }

    @Test
    protected void transfer_from_cd_to_savings_should_be_possible() {
        savingsDepositAmount = 2000;

        bank.deposit(SAVINGS_ID_0, savingsDepositAmount);
        bank.passTime(12);
        bank.transfer(CD_ID, SAVINGS_ID_0, 2000);

        assertEquals(0, bank.getAccount(CD_ID).getBalance());
        assertEquals(AccountTests.applyAPR(APR, savingsDepositAmount, 12) + AccountTests.applyAPR(APR, INITIAL_CD_BALANCE, 12 * 4), bank.getAccount(SAVINGS_ID_0).getBalance());
    }

    @Test
    protected void pass_time_should_apply_apr() {
        int months = 6;
        checkingDepositAmount = 1000;
        savingsDepositAmount = 2500;

        bank.deposit(CHECKING_ID_0, checkingDepositAmount);
        bank.deposit(SAVINGS_ID_0, savingsDepositAmount);
        bank.passTime(months);

        assertEquals(AccountTests.applyAPR(APR, checkingDepositAmount, months), bank.getAccount(CHECKING_ID_0).getBalance());
        assertEquals(AccountTests.applyAPR(APR, savingsDepositAmount, months), bank.getAccount(SAVINGS_ID_0).getBalance());
        assertEquals(AccountTests.applyAPR(APR, INITIAL_CD_BALANCE, months * 4), bank.getAccount(CD_ID).getBalance());
    }

    @Test
    protected void pass_time_when_balance_is_less_than_or_equal_to_100_should_apply_min_balance_fee_then_apr() {
        double minBalanceFee = bank.getMinBalanceFee();
        int months = 2;

        bank.deposit(CHECKING_ID_0, 75);
        bank.deposit(CHECKING_ID_1, 90);
        bank.deposit(SAVINGS_ID_0, 100);
        bank.deposit(SAVINGS_ID_1, 110);
        bank.passTime(months);

        assertEquals(passTime(APR, minBalanceFee, "Checking", 75, months), bank.getAccount(CHECKING_ID_0).getBalance());
        assertEquals(passTime(APR, minBalanceFee, "Checking", 90, months), bank.getAccount(CHECKING_ID_1).getBalance());
        assertEquals(passTime(APR, minBalanceFee, "Savings", 100, months), bank.getAccount(SAVINGS_ID_0).getBalance());
        assertEquals(passTime(APR, minBalanceFee, "Savings", 110, months), bank.getAccount(SAVINGS_ID_1).getBalance());
        assertEquals(passTime(APR, minBalanceFee, "CD", INITIAL_CD_BALANCE, months), bank.getAccount(CD_ID).getBalance());
    }

    @Test
    protected void pass_time_when_balance_is_0_should_remove_account() {
        double minBalanceFee = bank.getMinBalanceFee();
        int months = 2;

        bank.deposit(CHECKING_ID_1, 25);
        bank.deposit(SAVINGS_ID_0, 50);
        bank.deposit(SAVINGS_ID_1, 75);
        bank.passTime(months);

        assertFalse(bank.containsAccount(CHECKING_ID_0));
        assertFalse(bank.containsAccount(CHECKING_ID_1));
        assertTrue(bank.containsAccount(SAVINGS_ID_0));
        assertEquals(passTime(APR, minBalanceFee, "Savings", 50, months), bank.getAccount(SAVINGS_ID_0).getBalance());
        assertTrue(bank.containsAccount(SAVINGS_ID_1));
        assertEquals(passTime(APR, minBalanceFee, "Savings", 75, months), bank.getAccount(SAVINGS_ID_1).getBalance());
        assertTrue(bank.containsAccount(CD_ID));
        assertEquals(passTime(APR, minBalanceFee, "CD", INITIAL_CD_BALANCE, months), bank.getAccount(CD_ID).getBalance());
    }

    public static double passTime(double apr, double minBalanceFee, String accountType, double initialBalance, int months) {
        double balance = initialBalance;

        for (int i = 0; i < months; i++) {
            if (balance <= 100) {
                balance -= minBalanceFee;
            }

            if (accountType.equals("CD")) {
                balance = AccountTests.applyAPR(apr, balance, 4);
            } else {
                balance = AccountTests.applyAPR(apr, balance, 1);
            }
        }

        return balance;
    }

    @Test
    protected void id_should_be_unique_and_8_digits() {
        assertFalse(bank.isIDValid("00000010"));
        assertFalse(bank.isIDValid("10000000"));


        assertFalse(bank.isIDValid(""));
        assertFalse(bank.isIDValid("nuke"));
        assertFalse(bank.isIDValid("&^F%yg7"));

        assertFalse(bank.isIDValid("8G73mU*)"));

        assertFalse(bank.isIDValid("*H()j89j("));
        assertFalse(bank.isIDValid("h8920891hgH&*282j8"));


        assertFalse(bank.isIDValid("48"));
        assertFalse(bank.isIDValid("7834972"));

        assertTrue(bank.isIDValid("05793729"));
        assertTrue(bank.isIDValid("24793478"));

        assertFalse(bank.isIDValid("783447992"));
        assertFalse(bank.isIDValid("973957845729385729375"));
    }

    @Test
    protected void apr_should_be_between_0_and_10_inclusive() {
        assertFalse(bank.isAPRValid(-10));

        assertFalse(bank.isAPRValid(-1));
        assertTrue(bank.isAPRValid(0));
        assertTrue(bank.isAPRValid(1));

        assertTrue(bank.isAPRValid(5));
        assertTrue(bank.isAPRValid(6));

        assertTrue(bank.isAPRValid(9));
        assertTrue(bank.isAPRValid(10));
        assertFalse(bank.isAPRValid(11));

        assertFalse(bank.isAPRValid(20));
    }

    @Test
    protected void initial_cd_balance_should_be_between_1000_and_10000_inclusive() {
        assertFalse(bank.isInitialCDBalanceValid(-20000));
        assertFalse(bank.isInitialCDBalanceValid(0));


        assertFalse(bank.isInitialCDBalanceValid(100));

        assertFalse(bank.isInitialCDBalanceValid(900));
        assertTrue(bank.isInitialCDBalanceValid(1000));
        assertTrue(bank.isInitialCDBalanceValid(1100));

        assertTrue(bank.isInitialCDBalanceValid(5000));
        assertTrue(bank.isInitialCDBalanceValid(6000));

        assertTrue(bank.isInitialCDBalanceValid(9000));
        assertTrue(bank.isInitialCDBalanceValid(10000));
        assertFalse(bank.isInitialCDBalanceValid(11000));

        assertFalse(bank.isInitialCDBalanceValid(20000));
    }

    @Test
    protected void deposit_should_contain_id() {
        checkingDepositAmount = 1000;
        savingsDepositAmount = 1000;

        assertFalse(bank.isDepositValid("24793180", checkingDepositAmount));
        assertFalse(bank.isDepositValid("08243478", savingsDepositAmount));
        assertTrue(bank.isDepositValid(CHECKING_ID_0, checkingDepositAmount));
        assertTrue(bank.isDepositValid(CHECKING_ID_1, checkingDepositAmount));
        assertTrue(bank.isDepositValid(SAVINGS_ID_0, savingsDepositAmount));
        assertTrue(bank.isDepositValid(SAVINGS_ID_1, savingsDepositAmount));
    }

    @Test
    protected void deposit_checking_should_be_greater_than_0() {
        assertFalse(bank.isDepositValid(CHECKING_ID_0, -500));

        assertFalse(bank.isDepositValid(CHECKING_ID_1, -100));
        assertFalse(bank.isDepositValid(CHECKING_ID_0, 0));
        assertTrue(bank.isDepositValid(CHECKING_ID_1, 100));

        assertTrue(bank.isDepositValid(CHECKING_ID_0, 500));
    }

    @Test
    protected void deposit_checking_should_be_less_than_or_equal_to_1000() {
        assertTrue(bank.isDepositValid(CHECKING_ID_1, 600));

        assertTrue(bank.isDepositValid(CHECKING_ID_0, 900));
        assertTrue(bank.isDepositValid(CHECKING_ID_1, 1000));
        assertFalse(bank.isDepositValid(CHECKING_ID_0, 1100));

        assertFalse(bank.isDepositValid(CHECKING_ID_1, 2000));
    }

    @Test
    protected void deposit_savings_should_be_greater_than_0() {
        assertFalse(bank.isDepositValid(SAVINGS_ID_0, -1200));

        assertFalse(bank.isDepositValid(SAVINGS_ID_1, -50));
        assertFalse(bank.isDepositValid(SAVINGS_ID_0, 0));
        assertTrue(bank.isDepositValid(SAVINGS_ID_1, 50));

        assertTrue(bank.isDepositValid(SAVINGS_ID_0, 1200));
    }

    @Test
    protected void deposit_savings_should_be_less_than_or_equal_to_2500() {
        assertTrue(bank.isDepositValid(SAVINGS_ID_1, 1300));

        assertTrue(bank.isDepositValid(SAVINGS_ID_0, 2400));
        assertTrue(bank.isDepositValid(SAVINGS_ID_1, 2500));
        assertFalse(bank.isDepositValid(SAVINGS_ID_0, 2600));

        assertFalse(bank.isDepositValid(SAVINGS_ID_1, 5000));
    }

    @Test
    protected void deposit_cd_should_not_be_possible() {
        assertFalse(bank.isDepositValid(CD_ID, -1000));

        assertFalse(bank.isDepositValid(CD_ID, -100));
        assertFalse(bank.isDepositValid(CD_ID, 0));
        assertFalse(bank.isDepositValid(CD_ID, 100));

        assertFalse(bank.isDepositValid(CD_ID, 1200));
        assertFalse(bank.isDepositValid(CD_ID, 1300));

        assertFalse(bank.isDepositValid(CD_ID, 2400));
        assertFalse(bank.isDepositValid(CD_ID, 2500));
        assertFalse(bank.isDepositValid(CD_ID, 2600));

        assertFalse(bank.isDepositValid(CD_ID, 5000));
    }

    @Test
    protected void withdraw_should_contain_id() {
        checkingDepositAmount = 1000;
        savingsDepositAmount = 1000;
        checkingWithdrawAmount = 400;
        savingsWithdrawAmount = 400;
        cdWithdrawAmount = 2000;

        bank.deposit(CHECKING_ID_0, checkingDepositAmount);
        bank.deposit(CHECKING_ID_1, checkingDepositAmount);
        bank.deposit(SAVINGS_ID_0, savingsDepositAmount);
        bank.deposit(SAVINGS_ID_1, savingsDepositAmount);
        bank.passTime(12);

        assertFalse(bank.isWithdrawValid("34784792", checkingWithdrawAmount));
        assertFalse(bank.isWithdrawValid("34784792", cdWithdrawAmount));
        assertFalse(bank.isWithdrawValid("94280578", savingsWithdrawAmount));
        assertFalse(bank.isWithdrawValid("94280578", cdWithdrawAmount));
        assertTrue(bank.isWithdrawValid(CHECKING_ID_0, checkingWithdrawAmount));
        assertTrue(bank.isWithdrawValid(CHECKING_ID_1, checkingWithdrawAmount));
        assertTrue(bank.isWithdrawValid(SAVINGS_ID_0, savingsWithdrawAmount));
        assertTrue(bank.isWithdrawValid(SAVINGS_ID_1, savingsWithdrawAmount));
        assertTrue(bank.isWithdrawValid(CD_ID, cdWithdrawAmount));
    }

    @Test
    protected void withdraw_checking_should_be_greater_than_0() {
        assertFalse(bank.isWithdrawValid(CHECKING_ID_0, -1000));

        assertFalse(bank.isWithdrawValid(CHECKING_ID_1, -50));
        assertFalse(bank.isWithdrawValid(CHECKING_ID_0, 0));
        assertTrue(bank.isWithdrawValid(CHECKING_ID_1, 50));

        assertTrue(bank.isWithdrawValid(CHECKING_ID_0, 200));
    }

    @Test
    protected void withdraw_checking_should_be_less_than_or_equal_to_400() {
        assertTrue(bank.isWithdrawValid(CHECKING_ID_1, 100));

        assertTrue(bank.isWithdrawValid(CHECKING_ID_0, 300));
        assertTrue(bank.isWithdrawValid(CHECKING_ID_1, 400));
        assertFalse(bank.isWithdrawValid(CHECKING_ID_0, 500));

        assertFalse(bank.isWithdrawValid(CHECKING_ID_1, 1000));
    }

    @Test
    protected void withdraw_savings_twice_a_month_or_more_should_not_be_possible() {
        savingsDepositAmount = 1500;
        savingsWithdrawAmount = 500;

        bank.deposit(SAVINGS_ID_0, savingsDepositAmount);
        assertTrue(bank.isWithdrawValid(SAVINGS_ID_0, savingsWithdrawAmount));

        bank.passTime(1);
        bank.withdraw(SAVINGS_ID_0, savingsWithdrawAmount);
        assertFalse(bank.isWithdrawValid(SAVINGS_ID_0, savingsWithdrawAmount));

        bank.withdraw(SAVINGS_ID_0, savingsWithdrawAmount);
        bank.passTime(1);
        assertTrue(bank.isWithdrawValid(SAVINGS_ID_0, savingsWithdrawAmount));
    }

    @Test
    protected void withdraw_savings_should_be_greater_than_0() {
        assertFalse(bank.isWithdrawValid(SAVINGS_ID_0, -500));

        assertFalse(bank.isWithdrawValid(SAVINGS_ID_1, -100));
        assertFalse(bank.isWithdrawValid(SAVINGS_ID_0, 0));
        assertTrue(bank.isWithdrawValid(SAVINGS_ID_1, 100));

        assertTrue(bank.isWithdrawValid(SAVINGS_ID_0, 500));
    }

    @Test
    protected void withdraw_savings_should_be_less_than_or_equal_to_1000() {
        assertTrue(bank.isWithdrawValid(SAVINGS_ID_1, 600));

        assertTrue(bank.isWithdrawValid(SAVINGS_ID_0, 900));
        assertTrue(bank.isWithdrawValid(SAVINGS_ID_1, 1000));
        assertFalse(bank.isWithdrawValid(SAVINGS_ID_0, 1100));

        assertFalse(bank.isWithdrawValid(SAVINGS_ID_1, 2000));
    }

    @Test
    protected void withdraw_cd_before_12_month_should_be_possible() {
        for (int i = 0; i < 24; i++) {
            assertEquals(i >= 12, bank.isWithdrawValid(CD_ID, 2000));

            bank.passTime(1);
        }
    }

    @Test
    protected void withdraw_cd_should_be_greater_than_or_equal_to_balance() {
        int months = 12;
        cdWithdrawAmount = passTime(APR, bank.getMinBalanceFee(), "CD", INITIAL_CD_BALANCE, months);

        for (int i = 0; i < months; i++) {
            bank.passTime(1);
        }

        assertFalse(bank.isWithdrawValid(CD_ID, cdWithdrawAmount - 500));
        assertFalse(bank.isWithdrawValid(CD_ID, cdWithdrawAmount - 100));
        assertEquals(cdWithdrawAmount, bank.getAccount(CD_ID).getBalance());
        assertTrue(bank.isWithdrawValid(CD_ID, cdWithdrawAmount));
        assertTrue(bank.isWithdrawValid(CD_ID, cdWithdrawAmount + 100));
        assertTrue(bank.isWithdrawValid(CD_ID, cdWithdrawAmount + 500));
    }

    @Test
    protected void transfer_should_contain_unique_fromID_and_toID() {
        assertFalse(bank.isTransferValid("34782794", CHECKING_ID_1, 400));
        assertFalse(bank.isTransferValid(CHECKING_ID_0, "78344279", 400));
        assertFalse(bank.isTransferValid("53794289", "08344279", 400));

        assertFalse(bank.isTransferValid(CHECKING_ID_0, CHECKING_ID_0, 400));
        assertFalse(bank.isTransferValid(SAVINGS_ID_1, SAVINGS_ID_1, 1000));

        assertTrue(bank.isTransferValid(SAVINGS_ID_1, CHECKING_ID_1, 1000));
        assertTrue(bank.isTransferValid(CHECKING_ID_1, CHECKING_ID_0, 400));
    }

    @Test
    protected void transfer_from_checking_to_checking_should_be_greater_than_0() {
        assertFalse(bank.isTransferValid(CHECKING_ID_0, CHECKING_ID_1, -300));

        assertFalse(bank.isTransferValid(CHECKING_ID_0, CHECKING_ID_1, -100));
        assertFalse(bank.isTransferValid(CHECKING_ID_0, CHECKING_ID_1, 0));
        assertTrue(bank.isTransferValid(CHECKING_ID_0, CHECKING_ID_1, 100));

        assertTrue(bank.isTransferValid(CHECKING_ID_0, CHECKING_ID_1, 250));
    }

    @Test
    protected void transfer_from_checking_to_checking_should_be_less_than_or_equal_to_400() {
        assertTrue(bank.isTransferValid(CHECKING_ID_1, CHECKING_ID_0, 150));

        assertTrue(bank.isTransferValid(CHECKING_ID_1, CHECKING_ID_0, 300));
        assertTrue(bank.isTransferValid(CHECKING_ID_1, CHECKING_ID_0, 400));
        assertFalse(bank.isTransferValid(CHECKING_ID_1, CHECKING_ID_0, 500));

        assertFalse(bank.isTransferValid(CHECKING_ID_1, CHECKING_ID_0, 1000));
    }

    @Test
    protected void transfer_from_checking_to_savings_should_be_greater_than_0() {
        assertFalse(bank.isTransferValid(CHECKING_ID_0, SAVINGS_ID_1, -300));

        assertFalse(bank.isTransferValid(CHECKING_ID_0, SAVINGS_ID_1, -100));
        assertFalse(bank.isTransferValid(CHECKING_ID_0, SAVINGS_ID_1, 0));
        assertTrue(bank.isTransferValid(CHECKING_ID_0, SAVINGS_ID_1, 100));

        assertTrue(bank.isTransferValid(CHECKING_ID_0, SAVINGS_ID_1, 250));
    }

    @Test
    protected void transfer_from_checking_to_savings_should_be_less_than_or_equal_to_400() {
        assertTrue(bank.isTransferValid(CHECKING_ID_1, SAVINGS_ID_1, 150));

        assertTrue(bank.isTransferValid(CHECKING_ID_1, SAVINGS_ID_1, 300));
        assertTrue(bank.isTransferValid(CHECKING_ID_1, SAVINGS_ID_1, 400));
        assertFalse(bank.isTransferValid(CHECKING_ID_1, SAVINGS_ID_1, 500));

        assertFalse(bank.isTransferValid(CHECKING_ID_1, SAVINGS_ID_1, 1000));
    }

    @Test
    protected void transfer_from_savings_twice_a_month_or_more_should_not_be_possible() {
        checkingDepositAmount = 1000;
        savingsDepositAmount = 2000;
        transferAmount = 400;

        bank.deposit(CHECKING_ID_1, checkingDepositAmount);
        bank.deposit(SAVINGS_ID_1, savingsDepositAmount);
        assertTrue(bank.isTransferValid(SAVINGS_ID_1, CHECKING_ID_1, transferAmount));

        bank.passTime(1);
        bank.transfer(SAVINGS_ID_1, CHECKING_ID_1, transferAmount);
        assertFalse(bank.isTransferValid(SAVINGS_ID_1, CHECKING_ID_1, transferAmount));

        bank.passTime(1);
        bank.transfer(SAVINGS_ID_1, CHECKING_ID_1, transferAmount);
        bank.passTime(1);
        assertTrue(bank.isTransferValid(SAVINGS_ID_1, CHECKING_ID_1, transferAmount));
    }

    @Test
    protected void transfer_from_savings_to_checking_should_be_greater_than_0() {
        assertFalse(bank.isTransferValid(SAVINGS_ID_0, CHECKING_ID_0, -300));

        assertFalse(bank.isTransferValid(SAVINGS_ID_0, CHECKING_ID_0, -100));
        assertFalse(bank.isTransferValid(SAVINGS_ID_0, CHECKING_ID_0, 0));
        assertTrue(bank.isTransferValid(SAVINGS_ID_0, CHECKING_ID_0, 100));

        assertTrue(bank.isTransferValid(SAVINGS_ID_0, CHECKING_ID_0, 500));
    }

    @Test
    protected void transfer_from_savings_to_savings_should_be_less_than_or_equal_to_1000() {
        assertTrue(bank.isTransferValid(SAVINGS_ID_0, CHECKING_ID_1, 600));

        assertTrue(bank.isTransferValid(SAVINGS_ID_0, CHECKING_ID_1, 900));
        assertTrue(bank.isTransferValid(SAVINGS_ID_0, CHECKING_ID_1, 1000));
        assertFalse(bank.isTransferValid(SAVINGS_ID_0, CHECKING_ID_1, 1100));

        assertFalse(bank.isTransferValid(SAVINGS_ID_0, CHECKING_ID_1, 2000));
    }

    @Test
    protected void transfer_to_cd_should_not_be_possible() {
        assertFalse(bank.isTransferValid(CHECKING_ID_0, CD_ID, -1000));

        assertFalse(bank.isTransferValid(CHECKING_ID_1, CD_ID, -100));
        assertFalse(bank.isTransferValid(SAVINGS_ID_0, CD_ID, 0));
        assertFalse(bank.isTransferValid(SAVINGS_ID_1, CD_ID, 100));

        assertFalse(bank.isTransferValid(CHECKING_ID_0, CD_ID, 1200));
        assertFalse(bank.isTransferValid(CHECKING_ID_1, CD_ID, 1300));

        assertFalse(bank.isTransferValid(SAVINGS_ID_0, CD_ID, 2400));
        assertFalse(bank.isTransferValid(SAVINGS_ID_1, CD_ID, 2500));
        assertFalse(bank.isTransferValid(CHECKING_ID_0, CD_ID, 2600));

        assertFalse(bank.isTransferValid(SAVINGS_ID_1, CD_ID, 5000));
    }

    @Test
    protected void transfer_from_cd_to_savings_before_12_month_should_not_be_possible() {
        bank.deposit(SAVINGS_ID_0, 2500);
        for (int i = 0; i < 24; i++) {
            assertEquals(i >= 12, bank.isTransferValid(CD_ID, SAVINGS_ID_0, 2000));

            bank.passTime(1);
        }
    }

    @Test
    protected void transfer_from_cd_to_savings_should_be_greater_than_or_equal_to_balance() {
        int months = 12;
        cdWithdrawAmount = passTime(APR, bank.getMinBalanceFee(), "CD", INITIAL_CD_BALANCE, months);

        bank.deposit(SAVINGS_ID_1, 2500);
        bank.passTime(months);

        assertFalse(bank.isTransferValid(CD_ID, SAVINGS_ID_1, cdWithdrawAmount - 500));

        assertFalse(bank.isTransferValid(CD_ID, SAVINGS_ID_1, cdWithdrawAmount - 100));
        assertEquals(cdWithdrawAmount, bank.getAccount(CD_ID).getBalance());
        assertTrue(bank.isTransferValid(CD_ID, SAVINGS_ID_1, cdWithdrawAmount));
        assertTrue(bank.isTransferValid(CD_ID, SAVINGS_ID_1, cdWithdrawAmount + 100));

        assertTrue(bank.isTransferValid(CD_ID, SAVINGS_ID_1, cdWithdrawAmount + 500));
    }

    @Test
    protected void pass_time_should_be_between_1_and_60_inclusive() {
        assertFalse(bank.isPassTimeValid(-10));

        assertFalse(bank.isPassTimeValid(0));
        assertTrue(bank.isPassTimeValid(1));
        assertTrue(bank.isPassTimeValid(2));

        assertTrue(bank.isPassTimeValid(30));
        assertTrue(bank.isPassTimeValid(40));

        assertTrue(bank.isPassTimeValid(50));
        assertTrue(bank.isPassTimeValid(60));
        assertFalse(bank.isPassTimeValid(70));

        assertFalse(bank.isPassTimeValid(100));
    }
}
