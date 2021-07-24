package server.game.pushing.paper.ledgervalidator.bank;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.game.pushing.paper.ledgervalidator.bank.account.*;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static server.game.pushing.paper.ledgervalidator.bank.AccountTests.applyAPR;
import static server.game.pushing.paper.ledgervalidator.bank.Bank.getMinInitialCDBalance;
import static server.game.pushing.paper.ledgervalidator.bank.Bank.getMonthsPerYear;

public class BankTests {
    protected Bank bank;

    protected final String CHECKING_ID_0 = "34792497";
    protected final String CHECKING_ID_1 = "42793478";
    protected final String SAVINGS_ID_0 = "00000000";
    protected final String SAVINGS_ID_1 = "98340842";
    protected final String CD_ID_0 = "54873935";
    protected final String CD_ID_1 = "37599823";
    protected final double APR = 0.6;
    protected final double INITIAL_CD_BALANCE = getMinInitialCDBalance();

    @BeforeEach
    protected void setUp() {
        bank = new Bank(Arrays.asList(
                new Checking(CHECKING_ID_1, APR),
                new Savings(SAVINGS_ID_1, APR),
                new CD(CD_ID_1, APR, INITIAL_CD_BALANCE)
        ));
        bank.createChecking(CHECKING_ID_0, APR);
        bank.createSavings(SAVINGS_ID_0, APR);
        bank.createCD(CD_ID_0, APR, INITIAL_CD_BALANCE);
    }

    @Test
    protected void initialize_bank_should_have_0_accounts() {
        assertEquals(0, (new Bank()).getAccounts().size());
    }

    @Test
    protected void create_checking_should_be_possible() {
        Account account = bank.getAccount(CHECKING_ID_0);

        assertEquals(AccountType.Checking, account.getAccountType());
        assertEquals(CHECKING_ID_0, account.getID());
        assertEquals(APR, account.getAPR());
        assertEquals(0, account.getBalance());
    }

    @Test
    protected void create_savings_should_be_possible() {
        Account account = bank.getAccount(SAVINGS_ID_0);

        assertEquals(AccountType.Savings, account.getAccountType());
        assertEquals(SAVINGS_ID_0, account.getID());
        assertEquals(APR, account.getAPR());
        assertEquals(0, account.getBalance());
    }

    @Test
    protected void create_cd_should_be_possible() {
        Account account = bank.getAccount(CD_ID_0);

        assertEquals(AccountType.CD, account.getAccountType());
        assertEquals(CD_ID_0, account.getID());
        assertEquals(APR, account.getAPR());
        assertEquals(INITIAL_CD_BALANCE, account.getBalance());
    }

    @Test
    protected void deposit_checking_should_be_possible() {
        double checkingDepositAmount = Checking.getMaxDepositAmount();

        bank.deposit(CHECKING_ID_0, checkingDepositAmount);

        assertEquals(checkingDepositAmount, bank.getAccount(CHECKING_ID_0).getBalance());
    }

    @Test
    protected void deposit_savings_should_be_possible() {
        double savingsDepositAmount = Savings.getMaxDepositAmount();

        bank.deposit(SAVINGS_ID_0, savingsDepositAmount);

        assertEquals(savingsDepositAmount, bank.getAccount(SAVINGS_ID_0).getBalance());
    }

    @Test
    protected void withdraw_should_be_possible() {
        double checkingDepositAmount = Checking.getMaxWithdrawAmount();
        double savingsDepositAmount = Savings.getMaxWithdrawAmount();
        double checkingWithdrawAmount = checkingDepositAmount - 100;
        double savingsWithdrawAmount = savingsDepositAmount - 50;
        double cdWithdrawAmount = CD.getMaxWithdrawAmount();

        bank.deposit(CHECKING_ID_0, checkingDepositAmount);
        bank.deposit(SAVINGS_ID_0, savingsDepositAmount);
        bank.withdraw(CHECKING_ID_0, checkingWithdrawAmount);
        bank.withdraw(SAVINGS_ID_0, savingsWithdrawAmount);
        bank.withdraw(CD_ID_0, cdWithdrawAmount);

        assertEquals(checkingDepositAmount - checkingWithdrawAmount, bank.getAccount(CHECKING_ID_0).getBalance());
        assertEquals(savingsDepositAmount - savingsWithdrawAmount, bank.getAccount(SAVINGS_ID_0).getBalance());
        assertEquals(0, bank.getAccount(CD_ID_0).getBalance());
    }

    @Test
    protected void transfer_from_checking_to_checking_should_be_possible() {
        double checkingDepositAmount = Checking.getMaxDepositAmount();
        double transferAmount = 200;

        bank.deposit(CHECKING_ID_0, checkingDepositAmount);
        bank.transfer(CHECKING_ID_0, CHECKING_ID_1, transferAmount);

        assertEquals(checkingDepositAmount - transferAmount, bank.getAccount(CHECKING_ID_0).getBalance());
        assertEquals(transferAmount, bank.getAccount(CHECKING_ID_1).getBalance());
    }

    @Test
    protected void transfer_from_checking_to_savings_should_be_possible() {
        double checkingDepositAmount = Checking.getMaxDepositAmount();
        double transferAmount = 400;

        bank.deposit(CHECKING_ID_1, checkingDepositAmount);
        bank.transfer(CHECKING_ID_1, SAVINGS_ID_1, transferAmount);

        assertEquals(checkingDepositAmount - transferAmount, bank.getAccount(CHECKING_ID_1).getBalance());
        assertEquals(transferAmount, bank.getAccount(SAVINGS_ID_1).getBalance());
    }

    @Test
    protected void transfer_from_savings_to_checking_should_be_possible() {
        double savingsDepositAmount = Savings.getMaxDepositAmount();
        double transferAmount = 500;

        bank.deposit(SAVINGS_ID_0, savingsDepositAmount);
        bank.transfer(SAVINGS_ID_0, CHECKING_ID_0, transferAmount);

        assertEquals(savingsDepositAmount - transferAmount, bank.getAccount(SAVINGS_ID_0).getBalance());
        assertEquals(transferAmount, bank.getAccount(CHECKING_ID_0).getBalance());
    }

    @Test
    protected void transfer_from_savings_to_savings_should_be_possible() {
        double savingsDepositAmount = Savings.getMaxDepositAmount();
        double transferAmount = 1000;

        bank.deposit(SAVINGS_ID_1, savingsDepositAmount);
        bank.transfer(SAVINGS_ID_1, SAVINGS_ID_0, transferAmount);

        assertEquals(savingsDepositAmount - transferAmount, bank.getAccount(SAVINGS_ID_1).getBalance());
        assertEquals(transferAmount, bank.getAccount(SAVINGS_ID_0).getBalance());
    }

    @Test
    protected void transfer_from_cd_to_savings_should_be_possible() {
        double minBalanceFee = bank.getMinBalanceFee();
        int months = getMonthsPerYear();
        double savingsDepositAmount = Savings.getMaxDepositAmount();
        double transferAmount = Savings.getMaxDepositAmount();
        bank.deposit(SAVINGS_ID_0, savingsDepositAmount);
        bank.passTime(months);

        bank.transfer(CD_ID_0, SAVINGS_ID_0, transferAmount);

        assertEquals(0, bank.getAccount(CD_ID_0).getBalance());
        assertEquals(
                passTime(APR, minBalanceFee, AccountType.Savings, savingsDepositAmount, months)
                        + passTime(APR, minBalanceFee, AccountType.CD, INITIAL_CD_BALANCE, months),
                bank.getAccount(SAVINGS_ID_0).getBalance()
        );
    }

    @Test
    protected void transfer_when_less_than_or_equal_to_balance_should_be_possible() {
        double checkingWithdrawAmount = 400;
        double savingsWithdrawAmount = 700;
        bank.deposit(CHECKING_ID_1, checkingWithdrawAmount);
        bank.deposit(SAVINGS_ID_0, savingsWithdrawAmount);

        assertEquals(checkingWithdrawAmount, bank.getAccount(CHECKING_ID_1).getBalance());
        bank.transfer(CHECKING_ID_1, SAVINGS_ID_0, checkingWithdrawAmount);

        assertTrue(savingsWithdrawAmount < bank.getAccount(SAVINGS_ID_0).getBalance());
        bank.transfer(SAVINGS_ID_0, CHECKING_ID_1, savingsWithdrawAmount);

        assertEquals(savingsWithdrawAmount, bank.getAccount(CHECKING_ID_1).getBalance());
        assertEquals(checkingWithdrawAmount, bank.getAccount(SAVINGS_ID_0).getBalance());
    }

    @Test
    protected void transfer_when_greater_than_balance_should_transfer_amount_equal_to_balance() {
        double savingsDepositAmount = 100;
        double savingsWithdrawAmount = Savings.getMaxWithdrawAmount();
        bank.deposit(SAVINGS_ID_0, savingsDepositAmount);

        assertTrue(savingsWithdrawAmount > bank.getAccount(SAVINGS_ID_0).getBalance());
        bank.transfer(SAVINGS_ID_0, SAVINGS_ID_1, savingsWithdrawAmount);

        assertEquals(0, bank.getAccount(SAVINGS_ID_0).getBalance());
        assertEquals(savingsDepositAmount, bank.getAccount(SAVINGS_ID_1).getBalance());
    }

    @Test
    protected void pass_time_should_apply_apr() {
        double minBalanceFee = bank.getMinBalanceFee();
        int months = 6;
        double checkingDepositAmount = Checking.getMaxDepositAmount();
        double savingsDepositAmount = Savings.getMaxDepositAmount();
        bank.deposit(CHECKING_ID_0, checkingDepositAmount);
        bank.deposit(SAVINGS_ID_0, savingsDepositAmount);

        bank.passTime(months);

        assertEquals(passTime(APR, minBalanceFee, AccountType.Checking, checkingDepositAmount, months), bank.getAccount(CHECKING_ID_0).getBalance());
        assertEquals(passTime(APR, minBalanceFee, AccountType.Savings, savingsDepositAmount, months), bank.getAccount(SAVINGS_ID_0).getBalance());
        assertEquals(passTime(APR, minBalanceFee, AccountType.CD, INITIAL_CD_BALANCE, months), bank.getAccount(CD_ID_0).getBalance());
    }

    @Test
    protected void pass_time_when_balance_is_less_than_or_equal_to_100_should_apply_min_balance_fee_then_apr() {
        double minBalanceFee = bank.getMinBalanceFee();
        int months = 2;
        double checkingDepositAmount = 90;
        double savingsDepositAmount = 100;
        bank.deposit(CHECKING_ID_1, checkingDepositAmount);
        bank.deposit(SAVINGS_ID_0, savingsDepositAmount);

        bank.passTime(months);

        assertEquals(passTime(APR, minBalanceFee, AccountType.Checking, checkingDepositAmount, months), bank.getAccount(CHECKING_ID_1).getBalance());
        assertEquals(passTime(APR, minBalanceFee, AccountType.Savings, savingsDepositAmount, months), bank.getAccount(SAVINGS_ID_0).getBalance());
        assertEquals(passTime(APR, minBalanceFee, AccountType.CD, INITIAL_CD_BALANCE, months), bank.getAccount(CD_ID_0).getBalance());
    }

    @Test
    protected void pass_time_when_balance_is_0_should_remove_account() {
        double minBalanceFee = bank.getMinBalanceFee();
        int months = 2;
        double checkingDepositAmount = 25;
        double savingsDepositAmount = 50;
        bank.deposit(CHECKING_ID_1, checkingDepositAmount);
        bank.deposit(SAVINGS_ID_1, savingsDepositAmount);

        bank.passTime(months);

        assertFalse(bank.containsAccount(CHECKING_ID_0));
        assertFalse(bank.containsAccount(CHECKING_ID_1));
        assertFalse(bank.containsAccount(SAVINGS_ID_0));
        assertTrue(bank.containsAccount(SAVINGS_ID_1));
        assertEquals(passTime(APR, minBalanceFee, AccountType.Savings, savingsDepositAmount, months), bank.getAccount(SAVINGS_ID_1).getBalance());
        assertTrue(bank.containsAccount(CD_ID_0));
        assertEquals(passTime(APR, minBalanceFee, AccountType.CD, INITIAL_CD_BALANCE, months), bank.getAccount(CD_ID_0).getBalance());
    }

    public static double passTime(double apr, double minBalanceFee, AccountType accountType, double initialBalance, int months) {
        double balance = initialBalance;

        for (int i = 0; i < months; i++) {
            if (balance <= 100) {
                balance -= minBalanceFee;
            }

            if (accountType == AccountType.CD) {
                balance = applyAPR(apr, balance, 4);
            } else {
                balance = applyAPR(apr, balance, 1);
            }
        }

        return balance;
    }

    @Test
    protected void id_should_be_unique_and_8_digits() {
        assertFalse(bank.isIDValid("00000000"));

        assertFalse(bank.isIDValid(""));
        assertFalse(bank.isIDValid("y"));

        assertFalse(bank.isIDValid("kw%K8r&"));
        assertFalse(bank.isIDValid("df%KiUr*j"));
        assertFalse(bank.isIDValid("9g3gG&(3fO&*f3"));

        assertFalse(bank.isIDValid("welcomes"));
        assertFalse(bank.isIDValid("POWERFUL"));
        assertFalse(bank.isIDValid("$@^*#$*("));
        assertTrue(bank.isIDValid("78349722"));
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
        assertFalse(bank.isInitialCDBalanceValid(500));
        assertFalse(bank.isInitialCDBalanceValid(900));
        assertTrue(bank.isInitialCDBalanceValid(1000));
        assertTrue(bank.isInitialCDBalanceValid(1100));
        assertTrue(bank.isInitialCDBalanceValid(5000));

        assertTrue(bank.isInitialCDBalanceValid(6000));
        assertTrue(bank.isInitialCDBalanceValid(9000));
        assertTrue(bank.isInitialCDBalanceValid(10000));
        assertFalse(bank.isInitialCDBalanceValid(11000));
        assertFalse(bank.isInitialCDBalanceValid(20000));

        assertFalse(bank.isInitialCDBalanceValid(-1000));
        assertFalse(bank.isInitialCDBalanceValid(0));
    }

    @Test
    protected void deposit_should_contain_a_taken_id() {
        double savingsDepositAmount = Savings.getMaxDepositAmount();

        assertFalse(bank.isDepositAmountValid("08243478", savingsDepositAmount));
        assertTrue(bank.isDepositAmountValid(SAVINGS_ID_0, savingsDepositAmount));
    }

    @Test
    protected void deposit_checking_should_be_greater_than_0() {
        double checkingDepositAmount = 0;

        assertFalse(bank.isDepositAmountValid(CHECKING_ID_0, checkingDepositAmount - 500));
        assertFalse(bank.isDepositAmountValid(CHECKING_ID_1, checkingDepositAmount - 100));
        assertFalse(bank.isDepositAmountValid(CHECKING_ID_0, checkingDepositAmount));
        assertTrue(bank.isDepositAmountValid(CHECKING_ID_1, checkingDepositAmount + 100));
        assertTrue(bank.isDepositAmountValid(CHECKING_ID_0, 500));
    }

    @Test
    protected void deposit_checking_should_be_less_than_or_equal_to_1000() {
        double checkingDepositAmount = 1000;

        assertTrue(bank.isDepositAmountValid(CHECKING_ID_1, 600));
        assertTrue(bank.isDepositAmountValid(CHECKING_ID_0, checkingDepositAmount - 100));
        assertTrue(bank.isDepositAmountValid(CHECKING_ID_1, checkingDepositAmount));
        assertFalse(bank.isDepositAmountValid(CHECKING_ID_0, checkingDepositAmount + 100));
        assertFalse(bank.isDepositAmountValid(CHECKING_ID_1, checkingDepositAmount + 500));
    }

    @Test
    protected void deposit_savings_should_be_greater_than_0() {
        double savingsDepositAmount = 0;

        assertFalse(bank.isDepositAmountValid(SAVINGS_ID_0, savingsDepositAmount - 1000));
        assertFalse(bank.isDepositAmountValid(SAVINGS_ID_1, savingsDepositAmount - 50));
        assertFalse(bank.isDepositAmountValid(SAVINGS_ID_0, savingsDepositAmount));
        assertTrue(bank.isDepositAmountValid(SAVINGS_ID_1, savingsDepositAmount + 50));
        assertTrue(bank.isDepositAmountValid(SAVINGS_ID_0, 1200));
    }

    @Test
    protected void deposit_savings_should_be_less_than_or_equal_to_2500() {
        double savingsDepositAmount = 2500;

        assertTrue(bank.isDepositAmountValid(SAVINGS_ID_1, 1300));
        assertTrue(bank.isDepositAmountValid(SAVINGS_ID_0, savingsDepositAmount - 50));
        assertTrue(bank.isDepositAmountValid(SAVINGS_ID_1, savingsDepositAmount));
        assertFalse(bank.isDepositAmountValid(SAVINGS_ID_0, savingsDepositAmount + 50));
        assertFalse(bank.isDepositAmountValid(SAVINGS_ID_1, savingsDepositAmount + 1000));
    }

    @Test
    protected void deposit_cd_should_not_be_possible() {
        List<Double> depositAmounts = Arrays.asList(-1000.0, -50.0, 0.0, 50.0, 1200.0, 1300.0, 2400.0, 2500.0, 2550.0, 3500.0);

        for (Double depositAmount : depositAmounts) {
            assertFalse(bank.isDepositAmountValid(CD_ID_0, depositAmount));
        }
    }

    @Test
    protected void withdraw_should_contain_a_taken_id() {
        double checkingWithdrawAmount = Checking.getMaxWithdrawAmount();

        assertFalse(bank.isWithdrawAmountValid("34784792", checkingWithdrawAmount));
        assertTrue(bank.isWithdrawAmountValid(CHECKING_ID_0, checkingWithdrawAmount));
    }

    @Test
    protected void withdraw_checking_should_be_greater_than_0() {
        double checkingWithdrawAmount = 0;

        assertFalse(bank.isWithdrawAmountValid(CHECKING_ID_0, checkingWithdrawAmount - 1000));
        assertFalse(bank.isWithdrawAmountValid(CHECKING_ID_1, checkingWithdrawAmount - 100));
        assertFalse(bank.isWithdrawAmountValid(CHECKING_ID_0, checkingWithdrawAmount));
        assertTrue(bank.isWithdrawAmountValid(CHECKING_ID_1, checkingWithdrawAmount + 100));
    }

    @Test
    protected void withdraw_checking_should_be_less_than_or_equal_to_400() {
        double checkingWithdrawAmount = 400;

        assertTrue(bank.isWithdrawAmountValid(CHECKING_ID_0, checkingWithdrawAmount - 100));
        assertTrue(bank.isWithdrawAmountValid(CHECKING_ID_1, checkingWithdrawAmount));
        assertFalse(bank.isWithdrawAmountValid(CHECKING_ID_0, checkingWithdrawAmount + 100));
        assertFalse(bank.isWithdrawAmountValid(CHECKING_ID_1, checkingWithdrawAmount + 1000));
    }

    @Test
    protected void withdraw_savings_should_be_possible_once_a_month() {
        double savingsDepositAmount = Savings.getMaxWithdrawAmount();
        double savingsWithdrawAmount = savingsDepositAmount - 100;
        bank.deposit(SAVINGS_ID_0, savingsDepositAmount);

        assertTrue(bank.isWithdrawAmountValid(SAVINGS_ID_0, savingsWithdrawAmount));
        bank.withdraw(SAVINGS_ID_0, savingsWithdrawAmount);

        assertFalse(bank.isWithdrawAmountValid(SAVINGS_ID_0, savingsWithdrawAmount));

        bank.passTime(1);
        assertTrue(bank.isWithdrawAmountValid(SAVINGS_ID_0, savingsWithdrawAmount));
    }

    @Test
    protected void withdraw_savings_should_be_greater_than_0() {
        double savingsWithdrawAmount = 0;

        assertFalse(bank.isWithdrawAmountValid(SAVINGS_ID_0, savingsWithdrawAmount - 500));
        assertFalse(bank.isWithdrawAmountValid(SAVINGS_ID_1, savingsWithdrawAmount - 50));
        assertFalse(bank.isWithdrawAmountValid(SAVINGS_ID_0, savingsWithdrawAmount));
        assertTrue(bank.isWithdrawAmountValid(SAVINGS_ID_1, savingsWithdrawAmount + 50));
        assertTrue(bank.isWithdrawAmountValid(SAVINGS_ID_0, 500));
    }

    @Test
    protected void withdraw_savings_should_be_less_than_or_equal_to_1000() {
        double savingsWithdrawAmount = 1000;

        assertTrue(bank.isWithdrawAmountValid(SAVINGS_ID_1, 600));
        assertTrue(bank.isWithdrawAmountValid(SAVINGS_ID_0, savingsWithdrawAmount - 50));
        assertTrue(bank.isWithdrawAmountValid(SAVINGS_ID_1, savingsWithdrawAmount));
        assertFalse(bank.isWithdrawAmountValid(SAVINGS_ID_0, savingsWithdrawAmount + 50));
        assertFalse(bank.isWithdrawAmountValid(SAVINGS_ID_1, savingsWithdrawAmount + 500));
    }

    @Test
    protected void withdraw_cd_should_be_possible_after_a_year_inclusive() {
        for (int i = 0; i < getMonthsPerYear() + 12; i++) {
            assertEquals(i >= getMonthsPerYear(), bank.isWithdrawAmountValid(CD_ID_0, CD.getMaxWithdrawAmount()));

            bank.passTime(1);
        }
    }

    @Test
    protected void withdraw_cd_should_be_greater_than_or_equal_to_balance() {
        int months = getMonthsPerYear();
        double cdWithdrawAmount = passTime(APR, bank.getMinBalanceFee(), AccountType.CD, INITIAL_CD_BALANCE, months);

        bank.passTime(months);

        assertEquals(cdWithdrawAmount, bank.getAccount(CD_ID_0).getBalance());
        assertFalse(bank.isWithdrawAmountValid(CD_ID_0, cdWithdrawAmount - 1000));
        assertFalse(bank.isWithdrawAmountValid(CD_ID_0, cdWithdrawAmount - 100));
        assertTrue(bank.isWithdrawAmountValid(CD_ID_0, cdWithdrawAmount));
        assertTrue(bank.isWithdrawAmountValid(CD_ID_0, cdWithdrawAmount + 100));
        assertTrue(bank.isWithdrawAmountValid(CD_ID_0, cdWithdrawAmount + 1000));

        assertFalse(bank.isWithdrawAmountValid(CD_ID_0, -1000));
        assertFalse(bank.isWithdrawAmountValid(CD_ID_0, 0));
        assertTrue(bank.isWithdrawAmountValid(CD_ID_0, Double.POSITIVE_INFINITY));
    }

    @Test
    protected void transfer_should_contain_unique_and_taken_fromID_and_toID() {
        double transferAmount = 400;

        assertFalse(bank.isTransferAmountValid(CHECKING_ID_0, CHECKING_ID_0, transferAmount));
        assertFalse(bank.isTransferAmountValid("34782794", CHECKING_ID_1, transferAmount));
        assertFalse(bank.isTransferAmountValid(CHECKING_ID_1, "78344279", transferAmount));
        assertTrue(bank.isTransferAmountValid(CHECKING_ID_0, CHECKING_ID_1, transferAmount));
    }

    @Test
    protected void transfer_from_checking_to_checking_should_be_greater_than_0() {
        double transferAmount = 0;

        assertFalse(bank.isTransferAmountValid(CHECKING_ID_0, CHECKING_ID_1, transferAmount - 500));
        assertFalse(bank.isTransferAmountValid(CHECKING_ID_0, CHECKING_ID_1, transferAmount - 50));
        assertFalse(bank.isTransferAmountValid(CHECKING_ID_0, CHECKING_ID_1, transferAmount));
        assertTrue(bank.isTransferAmountValid(CHECKING_ID_0, CHECKING_ID_1, transferAmount + 50));
    }

    @Test
    protected void transfer_from_checking_to_checking_should_be_less_than_or_equal_to_400() {
        double transferAmount = 400;

        assertTrue(bank.isTransferAmountValid(CHECKING_ID_1, CHECKING_ID_0, transferAmount - 50));
        assertTrue(bank.isTransferAmountValid(CHECKING_ID_1, CHECKING_ID_0, transferAmount));
        assertFalse(bank.isTransferAmountValid(CHECKING_ID_1, CHECKING_ID_0, transferAmount + 50));
        assertFalse(bank.isTransferAmountValid(CHECKING_ID_1, CHECKING_ID_0, transferAmount + 500));
    }

    @Test
    protected void transfer_from_checking_to_savings_should_be_greater_than_0() {
        double transferAmount = 0;

        assertFalse(bank.isTransferAmountValid(CHECKING_ID_0, SAVINGS_ID_1, transferAmount - 1000));
        assertFalse(bank.isTransferAmountValid(CHECKING_ID_0, SAVINGS_ID_1, transferAmount - 100));
        assertFalse(bank.isTransferAmountValid(CHECKING_ID_0, SAVINGS_ID_1, transferAmount));
        assertTrue(bank.isTransferAmountValid(CHECKING_ID_0, SAVINGS_ID_1, transferAmount + 100));
    }

    @Test
    protected void transfer_from_checking_to_savings_should_be_less_than_or_equal_to_400() {
        double transferAmount = 400;

        assertTrue(bank.isTransferAmountValid(CHECKING_ID_1, SAVINGS_ID_1, transferAmount - 100));
        assertTrue(bank.isTransferAmountValid(CHECKING_ID_1, SAVINGS_ID_1, transferAmount));
        assertFalse(bank.isTransferAmountValid(CHECKING_ID_1, SAVINGS_ID_1, transferAmount + 100));
        assertFalse(bank.isTransferAmountValid(CHECKING_ID_1, SAVINGS_ID_1, transferAmount + 1000));
    }

    @Test
    protected void transfer_from_savings_twice_a_month_or_more_should_not_be_possible() {
        double checkingDepositAmount = Checking.getMaxDepositAmount();
        double savingsDepositAmount = Savings.getMaxDepositAmount();
        double transferAmount = 400;
        bank.deposit(CHECKING_ID_1, checkingDepositAmount);
        bank.deposit(SAVINGS_ID_1, savingsDepositAmount);

        assertTrue(bank.isTransferAmountValid(SAVINGS_ID_1, CHECKING_ID_1, transferAmount));
        bank.transfer(SAVINGS_ID_1, CHECKING_ID_1, transferAmount);

        assertFalse(bank.isTransferAmountValid(SAVINGS_ID_1, CHECKING_ID_1, transferAmount));

        bank.passTime(1);
        assertTrue(bank.isTransferAmountValid(SAVINGS_ID_1, CHECKING_ID_1, transferAmount));
    }

    @Test
    protected void transfer_from_savings_to_checking_should_be_greater_than_0() {
        double transferAmount = 0;

        assertFalse(bank.isTransferAmountValid(SAVINGS_ID_0, CHECKING_ID_0, transferAmount - 500));
        assertFalse(bank.isTransferAmountValid(SAVINGS_ID_0, CHECKING_ID_0, transferAmount - 50));
        assertFalse(bank.isTransferAmountValid(SAVINGS_ID_0, CHECKING_ID_0, transferAmount));
        assertTrue(bank.isTransferAmountValid(SAVINGS_ID_0, CHECKING_ID_0, transferAmount + 50));
        assertTrue(bank.isTransferAmountValid(SAVINGS_ID_0, CHECKING_ID_0, 500));
    }

    @Test
    protected void transfer_from_savings_to_checking_should_be_less_than_or_equal_to_1000() {
        double transferAmount = 1000;

        assertTrue(bank.isTransferAmountValid(SAVINGS_ID_0, CHECKING_ID_1, 600));
        assertTrue(bank.isTransferAmountValid(SAVINGS_ID_0, CHECKING_ID_1, transferAmount - 50));
        assertTrue(bank.isTransferAmountValid(SAVINGS_ID_0, CHECKING_ID_1, transferAmount));
        assertFalse(bank.isTransferAmountValid(SAVINGS_ID_0, CHECKING_ID_1, transferAmount + 50));
        assertFalse(bank.isTransferAmountValid(SAVINGS_ID_0, CHECKING_ID_1, transferAmount + 500));
    }

    @Test
    protected void transfer_from_savings_to_savings_should_be_greater_than_0() {
        double transferAmount = 0;

        assertFalse(bank.isTransferAmountValid(SAVINGS_ID_0, SAVINGS_ID_1, transferAmount - 500));
        assertFalse(bank.isTransferAmountValid(SAVINGS_ID_0, SAVINGS_ID_1, transferAmount - 50));
        assertFalse(bank.isTransferAmountValid(SAVINGS_ID_0, SAVINGS_ID_1, transferAmount));
        assertTrue(bank.isTransferAmountValid(SAVINGS_ID_0, SAVINGS_ID_1, transferAmount + 50));
        assertTrue(bank.isTransferAmountValid(SAVINGS_ID_0, SAVINGS_ID_1, 500));
    }

    @Test
    protected void transfer_from_savings_to_savings_should_be_less_than_or_equal_to_1000() {
        double transferAmount = 1000;

        assertTrue(bank.isTransferAmountValid(SAVINGS_ID_0, SAVINGS_ID_1, 600));
        assertTrue(bank.isTransferAmountValid(SAVINGS_ID_0, SAVINGS_ID_1, transferAmount - 50));
        assertTrue(bank.isTransferAmountValid(SAVINGS_ID_0, SAVINGS_ID_1, transferAmount));
        assertFalse(bank.isTransferAmountValid(SAVINGS_ID_0, SAVINGS_ID_1, transferAmount + 50));
        assertFalse(bank.isTransferAmountValid(SAVINGS_ID_0, SAVINGS_ID_1, transferAmount + 500));
    }

    @Test
    protected void transfer_to_cd_should_not_be_possible() {
        List<Double> transferAmounts = Arrays.asList(-500.0, -100.0, 0.0, 100.0, 1200.0, 1300.0, 2400.0, 2500.0, 2600.0, 3500.0);

        bank.passTime(getMonthsPerYear());

        for (Double transferAmount : transferAmounts) {
            assertFalse(bank.isTransferAmountValid(CHECKING_ID_1, CD_ID_0, transferAmount));
            assertFalse(bank.isTransferAmountValid(SAVINGS_ID_1, CD_ID_0, transferAmount));
            assertFalse(bank.isTransferAmountValid(CD_ID_1, CD_ID_0, transferAmount));
        }
    }

    @Test
    protected void transfer_from_cd_to_savings_should_be_possible_after_12_month_inclusive() {
        bank.deposit(SAVINGS_ID_0, Savings.getMaxDepositAmount());

        for (int i = 0; i < getMonthsPerYear() + 12; i++) {
            assertEquals(i >= getMonthsPerYear(), bank.isTransferAmountValid(CD_ID_0, SAVINGS_ID_0, Savings.getMaxDepositAmount()));

            bank.passTime(1);
        }
    }

    @Test
    protected void transfer_from_cd_to_savings_should_be_between_balance_and_2500_inclusive() {
        double cdAPR = 0.6;
        double initialCDBalance = 2200;
        int months = getMonthsPerYear();
        double savingsWithdrawAmount = Savings.getMaxDepositAmount();
        double cdWithdrawAmount = passTime(cdAPR, bank.getMinBalanceFee(), AccountType.CD, initialCDBalance, months);

        bank.removeAccount(CD_ID_0);
        bank.createCD(CD_ID_0, 0.6, initialCDBalance);
        bank.deposit(SAVINGS_ID_1, Savings.getMaxDepositAmount());
        bank.passTime(months);

        assertEquals(cdWithdrawAmount, bank.getAccount(CD_ID_0).getBalance());
        boolean isTransferFromCDToSavingsValid = cdWithdrawAmount <= savingsWithdrawAmount;
        assertTrue(isTransferFromCDToSavingsValid);

        assertFalse(bank.isTransferAmountValid(CD_ID_0, SAVINGS_ID_1, cdWithdrawAmount - 500));
        assertFalse(bank.isTransferAmountValid(CD_ID_0, SAVINGS_ID_1, cdWithdrawAmount - 50));
        assertTrue(bank.isTransferAmountValid(CD_ID_0, SAVINGS_ID_1, cdWithdrawAmount));
        assertTrue(bank.isTransferAmountValid(CD_ID_0, SAVINGS_ID_1, cdWithdrawAmount + 50));

        assertTrue(bank.isTransferAmountValid(CD_ID_0, SAVINGS_ID_1, savingsWithdrawAmount - 100));
        assertTrue(bank.isTransferAmountValid(CD_ID_0, SAVINGS_ID_1, savingsWithdrawAmount));
        assertFalse(bank.isTransferAmountValid(CD_ID_0, SAVINGS_ID_1, savingsWithdrawAmount + 100));
        assertFalse(bank.isTransferAmountValid(CD_ID_0, SAVINGS_ID_1, savingsWithdrawAmount + 1000));


        months = 60;
        cdWithdrawAmount = passTime(cdAPR, bank.getMinBalanceFee(), AccountType.CD, cdWithdrawAmount, months);

        bank.passTime(months);

        assertEquals(cdWithdrawAmount, bank.getAccount(CD_ID_0).getBalance());
        isTransferFromCDToSavingsValid = cdWithdrawAmount <= savingsWithdrawAmount;
        assertFalse(isTransferFromCDToSavingsValid);

        assertFalse(bank.isTransferAmountValid(CD_ID_0, SAVINGS_ID_1, cdWithdrawAmount - 500));
        assertFalse(bank.isTransferAmountValid(CD_ID_0, SAVINGS_ID_1, cdWithdrawAmount - 50));
        assertFalse(bank.isTransferAmountValid(CD_ID_0, SAVINGS_ID_1, cdWithdrawAmount));
        assertFalse(bank.isTransferAmountValid(CD_ID_0, SAVINGS_ID_1, cdWithdrawAmount + 50));

        assertFalse(bank.isTransferAmountValid(CD_ID_0, SAVINGS_ID_1, savingsWithdrawAmount - 100));
        assertFalse(bank.isTransferAmountValid(CD_ID_0, SAVINGS_ID_1, savingsWithdrawAmount));
        assertFalse(bank.isTransferAmountValid(CD_ID_0, SAVINGS_ID_1, savingsWithdrawAmount + 100));
        assertFalse(bank.isTransferAmountValid(CD_ID_0, SAVINGS_ID_1, savingsWithdrawAmount + 1000));
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
