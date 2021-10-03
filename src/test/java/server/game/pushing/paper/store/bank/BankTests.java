package server.game.pushing.paper.store.bank;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.game.pushing.paper.store.bank.account.Account;
import server.game.pushing.paper.store.bank.account.AccountType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.Math.min;
import static org.junit.jupiter.api.Assertions.*;
import static server.game.pushing.paper.store.bank.Bank.getMonthsPerYear;

public class BankTests {
    private Bank bank;

    private final String CHECKING_ID_0 = "34792497";
    private final String CHECKING_ID_1 = "42793478";
    private final String SAVINGS_ID_0 = "00000000";
    private final String SAVINGS_ID_1 = "98340842";
    private final String CD_ID_0 = "54873935";
    private final String CD_ID_1 = "37599823";
    private double apr;
    private double initialCDBalance;

    @BeforeEach
    protected void setUp() {
        bank = new Bank();
        apr = bank.getMaxAPR();
        initialCDBalance = bank.getMinInitialCDBalance();

        bank.createChecking(CHECKING_ID_0, apr);
        bank.createChecking(CHECKING_ID_1, apr);
        bank.createSavings(SAVINGS_ID_1, apr);
        bank.createSavings(SAVINGS_ID_0, apr);
        bank.createCD(CD_ID_1, apr, initialCDBalance);
        bank.createCD(CD_ID_0, apr, initialCDBalance);

        assertFalse(bank.isEmpty());
        assertEquals(6, bank.size());
        for (String id : bank.getIDs()) {
            assertTrue(bank.containsAccount(id));
        }
    }

    @Test
    protected void banks_should_start_with_0_accounts() {
        bank = new Bank();

        assertEquals(25, bank.getMinBalanceFee());
        assertEquals(10, bank.getMaxAPR());
        assertEquals(1000, bank.getMinInitialCDBalance());
        assertEquals(10000, bank.getMaxInitialCDBalance());
        assertEquals(60, bank.getMaxMonths());

        assertTrue(bank.isEmpty());
        assertEquals(0, bank.size());
    }

    @Test
    protected void banks_can_create_checking_accounts() {
        Account account = bank.getAccount(CHECKING_ID_0);
        assertEquals(AccountType.CHECKING, account.getAccountType());
        assertEquals(CHECKING_ID_0, account.getID());
        assertEquals(apr, account.getAPR());
        assertEquals(0, account.getBalance());
    }

    @Test
    protected void banks_can_create_savings_accounts() {
        Account account = bank.getAccount(SAVINGS_ID_0);
        assertEquals(AccountType.SAVINGS, account.getAccountType());
        assertEquals(SAVINGS_ID_0, account.getID());
        assertEquals(apr, account.getAPR());
        assertEquals(0, account.getBalance());
    }

    @Test
    protected void banks_can_create_cd_accounts() {
        Account account = bank.getAccount(CD_ID_0);
        assertEquals(AccountType.CD, account.getAccountType());
        assertEquals(CD_ID_0, account.getID());
        assertEquals(apr, account.getAPR());
        assertEquals(initialCDBalance, account.getBalance());
    }

    @Test
    protected void banks_can_deposit_to_checking_accounts() {
        String id = CHECKING_ID_0;
        double checkingDepositAmount = bank.getAccount(CHECKING_ID_1).getMaxDepositAmount();

        bank.deposit(id, checkingDepositAmount);

        assertEquals(checkingDepositAmount, bank.getAccount(id).getBalance());
    }

    @Test
    protected void banks_can_deposit_savings_accounts() {
        String id = SAVINGS_ID_0;
        double savingsDepositAmount = bank.getAccount(SAVINGS_ID_1).getMaxDepositAmount();

        bank.deposit(id, savingsDepositAmount);

        assertEquals(savingsDepositAmount, bank.getAccount(id).getBalance());
    }

    @Test
    protected void banks_can_withdraw_from_accounts() {
        double checkingDepositAmount = bank.getAccount(CHECKING_ID_1).getMaxDepositAmount();
        double savingsDepositAmount = bank.getAccount(SAVINGS_ID_1).getMaxDepositAmount();
        double checkingWithdrawAmount = bank.getAccount(CHECKING_ID_1).getMaxWithdrawAmount();
        double savingsWithdrawAmount = bank.getAccount(SAVINGS_ID_1).getMaxWithdrawAmount();
        double cdWithdrawAmount = bank.getAccount(CD_ID_1).getMaxWithdrawAmount();

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
    protected void banks_can_transfer_from_checking_to_checking() {
        String payingID = CHECKING_ID_0;
        String receivingID = CHECKING_ID_1;
        double checkingDepositAmount = bank.getAccount(CHECKING_ID_1).getMaxDepositAmount();
        double transferAmount = min(bank.getAccount(CHECKING_ID_1).getMaxWithdrawAmount(), bank.getAccount(CHECKING_ID_1).getMaxDepositAmount());

        bank.deposit(payingID, checkingDepositAmount);
        bank.transfer(payingID, receivingID, transferAmount);

        assertEquals(checkingDepositAmount - transferAmount, bank.getAccount(payingID).getBalance());
        assertEquals(transferAmount, bank.getAccount(receivingID).getBalance());
    }

    @Test
    protected void banks_can_transfer_from_checking_to_savings() {
        String payingID = CHECKING_ID_1;
        String receivingID = SAVINGS_ID_1;
        double checkingDepositAmount = bank.getAccount(CHECKING_ID_1).getMaxDepositAmount();
        double transferAmount = min(bank.getAccount(CHECKING_ID_1).getMaxWithdrawAmount(), bank.getAccount(SAVINGS_ID_1).getMaxDepositAmount());

        bank.deposit(payingID, checkingDepositAmount);
        bank.transfer(payingID, receivingID, transferAmount);

        assertEquals(checkingDepositAmount - transferAmount, bank.getAccount(payingID).getBalance());
        assertEquals(transferAmount, bank.getAccount(receivingID).getBalance());
    }

    @Test
    protected void banks_can_transfer_from_savings_to_checking() {
        String payingID = SAVINGS_ID_0;
        String receivingID = CHECKING_ID_0;
        double savingsDepositAmount = bank.getAccount(SAVINGS_ID_1).getMaxDepositAmount();
        double transferAmount = min(bank.getAccount(SAVINGS_ID_1).getMaxWithdrawAmount(), bank.getAccount(CHECKING_ID_1).getMaxDepositAmount());

        bank.deposit(payingID, savingsDepositAmount);
        bank.transfer(payingID, receivingID, transferAmount);

        assertEquals(savingsDepositAmount - transferAmount, bank.getAccount(payingID).getBalance());
        assertEquals(transferAmount, bank.getAccount(receivingID).getBalance());
    }

    @Test
    protected void banks_can_transfer_from_savings_to_savings() {
        String payingID = SAVINGS_ID_1;
        String receivingID = SAVINGS_ID_0;
        double savingsDepositAmount = bank.getAccount(SAVINGS_ID_1).getMaxDepositAmount();
        double transferAmount = min(bank.getAccount(SAVINGS_ID_1).getMaxWithdrawAmount(), bank.getAccount(SAVINGS_ID_1).getMaxDepositAmount());

        bank.deposit(payingID, savingsDepositAmount);
        bank.transfer(payingID, receivingID, transferAmount);

        assertEquals(savingsDepositAmount - transferAmount, bank.getAccount(payingID).getBalance());
        assertEquals(transferAmount, bank.getAccount(receivingID).getBalance());
    }

    @Test
    protected void banks_can_transfer_from_cd_to_savings() {
        double minBalanceFee = bank.getMinBalanceFee();
        int months = getMonthsPerYear();
        String payingID = CD_ID_0;
        String receivingID = SAVINGS_ID_0;
        double savingsDepositAmount = bank.getAccount(SAVINGS_ID_1).getMaxDepositAmount();
        double transferAmount = min(bank.getAccount(CD_ID_1).getMaxWithdrawAmount(), bank.getAccount(SAVINGS_ID_1).getMaxDepositAmount());
        bank.deposit(receivingID, savingsDepositAmount);
        bank.timeTravel(months);

        bank.transfer(payingID, receivingID, transferAmount);

        assertEquals(0, bank.getAccount(payingID).getBalance());
        assertEquals(
                passTime(minBalanceFee, months, savingsDepositAmount)
                        + passTime(minBalanceFee, months, initialCDBalance),
                bank.getAccount(receivingID).getBalance()
        );
    }

    @Test
    protected void banks_can_transfer_when_the_transfer_amount_is_less_than_or_equal_to_the_paying_account_balance() {
        String payingID = CHECKING_ID_1;
        String receivingID = SAVINGS_ID_0;
        double checkingWithdrawAmount = 400;
        double savingsWithdrawAmount = 700;
        bank.deposit(payingID, checkingWithdrawAmount);
        bank.deposit(receivingID, savingsWithdrawAmount);

        assertEquals(checkingWithdrawAmount, bank.getAccount(payingID).getBalance());
        bank.transfer(payingID, receivingID, checkingWithdrawAmount);

        assertTrue(savingsWithdrawAmount < bank.getAccount(receivingID).getBalance());
        bank.transfer(receivingID, payingID, savingsWithdrawAmount);

        assertEquals(savingsWithdrawAmount, bank.getAccount(payingID).getBalance());
        assertEquals(checkingWithdrawAmount, bank.getAccount(receivingID).getBalance());
    }

    @Test
    protected void banks_should_transfer_the_paying_account_balance_when_the_transfer_amount_is_greater_than_the_paying_account_balance() {
        String payingID = SAVINGS_ID_0;
        String receivingID = SAVINGS_ID_1;
        double savingsDepositAmount = 100;
        double savingsWithdrawAmount = bank.getAccount(SAVINGS_ID_1).getMaxWithdrawAmount();
        bank.deposit(payingID, savingsDepositAmount);

        assertTrue(savingsWithdrawAmount > bank.getAccount(payingID).getBalance());
        bank.transfer(payingID, receivingID, savingsWithdrawAmount);

        assertEquals(0, bank.getAccount(payingID).getBalance());
        assertEquals(savingsDepositAmount, bank.getAccount(receivingID).getBalance());
    }

    @Test
    protected void pass_time_when_balance_is_less_than_or_equal_to_100_should_apply_min_balance_fee() {
        double minBalanceFee = bank.getMinBalanceFee();
        int months = 2;
        double checkingDepositAmount = 90;
        double savingsDepositAmount = 100;
        bank.deposit(CHECKING_ID_1, checkingDepositAmount);
        bank.deposit(SAVINGS_ID_0, savingsDepositAmount);

        bank.timeTravel(months);

        assertEquals(passTime(minBalanceFee, months, checkingDepositAmount), bank.getAccount(CHECKING_ID_1).getBalance());
        assertEquals(passTime(minBalanceFee, months, savingsDepositAmount), bank.getAccount(SAVINGS_ID_0).getBalance());
        assertEquals(passTime(minBalanceFee, months, initialCDBalance), bank.getAccount(CD_ID_0).getBalance());
    }

    public static double passTime(double minBalanceFee, int months, double initialBalance) {
        double finalBalance = initialBalance;

        for (int i = 0; i < months; i++) {
            if (finalBalance <= 100) {
                finalBalance -= minBalanceFee;
            }
        }

        return finalBalance;
    }

    public static double applyAPR(double apr, double finalBalance) {
        return apr * finalBalance / getMonthsPerYear() / 100;
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
        double savingsDepositAmount = bank.getAccount(SAVINGS_ID_1).getMaxDepositAmount();

        assertFalse(bank.isDepositAmountValid("08243478", savingsDepositAmount));
        assertTrue(bank.isDepositAmountValid(SAVINGS_ID_0, savingsDepositAmount));
    }

    @Test
    protected void deposit_checking_should_be_greater_than_0() {
        String id = CHECKING_ID_1;
        double checkingDepositAmount = 0;

        assertFalse(bank.isDepositAmountValid(id, checkingDepositAmount - 500));
        assertFalse(bank.isDepositAmountValid(id, checkingDepositAmount - 100));
        assertFalse(bank.isDepositAmountValid(id, checkingDepositAmount));
        assertTrue(bank.isDepositAmountValid(id, checkingDepositAmount + 100));
        assertTrue(bank.isDepositAmountValid(id, 500));
    }

    @Test
    protected void deposit_checking_should_be_less_than_or_equal_to_1000() {
        String id = CHECKING_ID_1;
        double checkingDepositAmount = 1000;

        assertTrue(bank.isDepositAmountValid(id, 600));
        assertTrue(bank.isDepositAmountValid(id, checkingDepositAmount - 100));
        assertTrue(bank.isDepositAmountValid(id, checkingDepositAmount));
        assertFalse(bank.isDepositAmountValid(id, checkingDepositAmount + 100));
        assertFalse(bank.isDepositAmountValid(id, checkingDepositAmount + 500));
    }

    @Test
    protected void deposit_savings_should_be_greater_than_0() {
        String id = SAVINGS_ID_1;
        double savingsDepositAmount = 0;

        assertFalse(bank.isDepositAmountValid(id, savingsDepositAmount - 1000));
        assertFalse(bank.isDepositAmountValid(id, savingsDepositAmount - 50));
        assertFalse(bank.isDepositAmountValid(id, savingsDepositAmount));
        assertTrue(bank.isDepositAmountValid(id, savingsDepositAmount + 50));
        assertTrue(bank.isDepositAmountValid(id, 1200));
    }

    @Test
    protected void deposit_savings_should_be_less_than_or_equal_to_2500() {
        String id = SAVINGS_ID_0;
        double savingsDepositAmount = 2500;

        assertTrue(bank.isDepositAmountValid(id, 1300));
        assertTrue(bank.isDepositAmountValid(id, savingsDepositAmount - 50));
        assertTrue(bank.isDepositAmountValid(id, savingsDepositAmount));
        assertFalse(bank.isDepositAmountValid(id, savingsDepositAmount + 50));
        assertFalse(bank.isDepositAmountValid(id, savingsDepositAmount + 1000));
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
        double checkingWithdrawAmount = bank.getAccount(CHECKING_ID_1).getMaxWithdrawAmount();

        assertFalse(bank.isWithdrawAmountValid("34784792", checkingWithdrawAmount));
        assertTrue(bank.isWithdrawAmountValid(CHECKING_ID_0, checkingWithdrawAmount));
    }

    @Test
    protected void withdraw_checking_should_be_greater_than_0() {
        String id = CHECKING_ID_0;
        double checkingWithdrawAmount = 0;

        assertFalse(bank.isWithdrawAmountValid(id, checkingWithdrawAmount - 1000));
        assertFalse(bank.isWithdrawAmountValid(id, checkingWithdrawAmount - 100));
        assertFalse(bank.isWithdrawAmountValid(id, checkingWithdrawAmount));
        assertTrue(bank.isWithdrawAmountValid(id, checkingWithdrawAmount + 100));
    }

    @Test
    protected void withdraw_checking_should_be_less_than_or_equal_to_400() {
        String id = CHECKING_ID_1;
        double checkingWithdrawAmount = 400;

        assertTrue(bank.isWithdrawAmountValid(id, checkingWithdrawAmount - 100));
        assertTrue(bank.isWithdrawAmountValid(id, checkingWithdrawAmount));
        assertFalse(bank.isWithdrawAmountValid(id, checkingWithdrawAmount + 100));
        assertFalse(bank.isWithdrawAmountValid(id, checkingWithdrawAmount + 1000));
    }

    @Test
    protected void withdraw_savings_should_not_be_possible_twice_a_month_or_more() {
        String id = SAVINGS_ID_0;
        double savingsDepositAmount = bank.getAccount(SAVINGS_ID_1).getMaxWithdrawAmount();
        double savingsWithdrawAmount = savingsDepositAmount - 100;
        bank.deposit(id, savingsDepositAmount);

        assertTrue(bank.isWithdrawAmountValid(id, savingsWithdrawAmount));
        bank.withdraw(id, savingsWithdrawAmount);

        assertFalse(bank.isWithdrawAmountValid(id, savingsWithdrawAmount));

        bank.timeTravel(1);
        assertTrue(bank.isWithdrawAmountValid(id, savingsWithdrawAmount));
    }

    @Test
    protected void withdraw_savings_should_be_greater_than_0() {
        String id = SAVINGS_ID_1;
        double savingsWithdrawAmount = 0;

        assertFalse(bank.isWithdrawAmountValid(id, savingsWithdrawAmount - 500));
        assertFalse(bank.isWithdrawAmountValid(id, savingsWithdrawAmount - 50));
        assertFalse(bank.isWithdrawAmountValid(id, savingsWithdrawAmount));
        assertTrue(bank.isWithdrawAmountValid(id, savingsWithdrawAmount + 50));
        assertTrue(bank.isWithdrawAmountValid(id, 500));
    }

    @Test
    protected void withdraw_savings_should_be_less_than_or_equal_to_1000() {
        String id = SAVINGS_ID_0;
        double savingsWithdrawAmount = 1000;

        assertTrue(bank.isWithdrawAmountValid(id, 600));
        assertTrue(bank.isWithdrawAmountValid(id, savingsWithdrawAmount - 50));
        assertTrue(bank.isWithdrawAmountValid(id, savingsWithdrawAmount));
        assertFalse(bank.isWithdrawAmountValid(id, savingsWithdrawAmount + 50));
        assertFalse(bank.isWithdrawAmountValid(id, savingsWithdrawAmount + 500));
    }

    @Test
    protected void withdraw_cd_after_a_year_inclusive() {
        int monthsPerYear = getMonthsPerYear();

        for (int month = 0; month < monthsPerYear * 2; month++) {
            assertEquals(month >= getMonthsPerYear(), bank.isWithdrawAmountValid(CD_ID_0, bank.getAccount(CD_ID_1).getMaxWithdrawAmount()));

            bank.timeTravel(1);
        }
    }

    @Test
    protected void withdraw_cd_should_be_greater_than_or_equal_to_balance() {
        int months = getMonthsPerYear();
        String id = CD_ID_0;
        double cdWithdrawAmount = passTime(bank.getMinBalanceFee(), months, initialCDBalance);

        bank.timeTravel(months);

        assertEquals(cdWithdrawAmount, bank.getAccount(id).getBalance());
        assertFalse(bank.isWithdrawAmountValid(id, cdWithdrawAmount - 1000));
        assertFalse(bank.isWithdrawAmountValid(id, cdWithdrawAmount - 100));
        assertTrue(bank.isWithdrawAmountValid(id, cdWithdrawAmount));
        assertTrue(bank.isWithdrawAmountValid(id, cdWithdrawAmount + 100));
        assertTrue(bank.isWithdrawAmountValid(id, cdWithdrawAmount + 1000));

        assertFalse(bank.isWithdrawAmountValid(id, -1000));
        assertFalse(bank.isWithdrawAmountValid(id, 0));
        assertTrue(bank.isWithdrawAmountValid(id, Double.POSITIVE_INFINITY));
    }

    @Test
    protected void transfer_should_contain_unique_and_taken_from_id_and_to_id() {
        String payingID = CHECKING_ID_1;
        String receivingID = CHECKING_ID_0;
        double transferAmount = min(bank.getAccount(CHECKING_ID_1).getMaxWithdrawAmount(), bank.getAccount(CHECKING_ID_1).getMaxDepositAmount());

        assertFalse(bank.isTransferAmountValid(payingID, payingID, transferAmount));
        assertFalse(bank.isTransferAmountValid("34782794", receivingID, transferAmount));
        assertFalse(bank.isTransferAmountValid(payingID, "78344279", transferAmount));
        assertTrue(bank.isTransferAmountValid(payingID, receivingID, transferAmount));
    }

    @Test
    protected void transfer_from_checking_to_checking_should_be_greater_than_0() {
        String payingID = CHECKING_ID_0;
        String receivingID = CHECKING_ID_1;
        double transferAmount = 0;

        assertFalse(bank.isTransferAmountValid(payingID, receivingID, transferAmount - 500));
        assertFalse(bank.isTransferAmountValid(payingID, receivingID, transferAmount - 50));
        assertFalse(bank.isTransferAmountValid(payingID, receivingID, transferAmount));
        assertTrue(bank.isTransferAmountValid(payingID, receivingID, transferAmount + 50));
    }

    @Test
    protected void transfer_from_checking_to_checking_should_be_less_than_or_equal_to_400() {
        String payingID = CHECKING_ID_1;
        String receivingID = CHECKING_ID_0;
        double transferAmount = 400;

        assertTrue(bank.isTransferAmountValid(payingID, receivingID, transferAmount - 50));
        assertTrue(bank.isTransferAmountValid(payingID, receivingID, transferAmount));
        assertFalse(bank.isTransferAmountValid(payingID, receivingID, transferAmount + 50));
        assertFalse(bank.isTransferAmountValid(payingID, receivingID, transferAmount + 500));
    }

    @Test
    protected void transfer_from_checking_to_savings_should_be_greater_than_0() {
        String payingID = CHECKING_ID_1;
        String receivingID = SAVINGS_ID_0;
        double transferAmount = 0;

        assertFalse(bank.isTransferAmountValid(payingID, receivingID, transferAmount - 1000));
        assertFalse(bank.isTransferAmountValid(payingID, receivingID, transferAmount - 100));
        assertFalse(bank.isTransferAmountValid(payingID, receivingID, transferAmount));
        assertTrue(bank.isTransferAmountValid(payingID, receivingID, transferAmount + 100));
    }

    @Test
    protected void transfer_from_checking_to_savings_should_be_less_than_or_equal_to_400() {
        String payingID = CHECKING_ID_1;
        String receivingID = SAVINGS_ID_1;
        double transferAmount = 400;

        assertTrue(bank.isTransferAmountValid(payingID, receivingID, transferAmount - 100));
        assertTrue(bank.isTransferAmountValid(payingID, receivingID, transferAmount));
        assertFalse(bank.isTransferAmountValid(payingID, receivingID, transferAmount + 100));
        assertFalse(bank.isTransferAmountValid(payingID, receivingID, transferAmount + 1000));
    }

    @Test
    protected void transfer_from_savings_should_not_be_possible_twice_a_month_or_more() {
        String payingID = SAVINGS_ID_1;
        String receivingID = CHECKING_ID_1;
        double checkingDepositAmount = bank.getAccount(CHECKING_ID_1).getMaxDepositAmount();
        double savingsDepositAmount = bank.getAccount(SAVINGS_ID_1).getMaxDepositAmount();
        double transferAmount = min(bank.getAccount(CHECKING_ID_1).getMaxWithdrawAmount(), bank.getAccount(SAVINGS_ID_1).getMaxDepositAmount());
        bank.deposit(payingID, savingsDepositAmount);
        bank.deposit(receivingID, checkingDepositAmount);

        assertTrue(bank.isTransferAmountValid(payingID, receivingID, transferAmount));
        bank.transfer(payingID, receivingID, transferAmount);

        assertFalse(bank.isTransferAmountValid(payingID, receivingID, transferAmount));

        bank.timeTravel(1);
        assertTrue(bank.isTransferAmountValid(payingID, receivingID, transferAmount));
    }

    @Test
    protected void transfer_from_savings_to_checking_should_be_greater_than_0() {
        String payingID = SAVINGS_ID_0;
        String receivingID = CHECKING_ID_0;
        double transferAmount = 0;

        assertFalse(bank.isTransferAmountValid(payingID, receivingID, transferAmount - 500));
        assertFalse(bank.isTransferAmountValid(payingID, receivingID, transferAmount - 50));
        assertFalse(bank.isTransferAmountValid(payingID, receivingID, transferAmount));
        assertTrue(bank.isTransferAmountValid(payingID, receivingID, transferAmount + 50));
        assertTrue(bank.isTransferAmountValid(payingID, receivingID, 500));
    }

    @Test
    protected void transfer_from_savings_to_checking_should_be_less_than_or_equal_to_1000() {
        String payingID = SAVINGS_ID_0;
        String receivingID = CHECKING_ID_1;
        double transferAmount = 1000;

        assertTrue(bank.isTransferAmountValid(payingID, receivingID, 600));
        assertTrue(bank.isTransferAmountValid(payingID, receivingID, transferAmount - 50));
        assertTrue(bank.isTransferAmountValid(payingID, receivingID, transferAmount));
        assertFalse(bank.isTransferAmountValid(payingID, receivingID, transferAmount + 50));
        assertFalse(bank.isTransferAmountValid(payingID, receivingID, transferAmount + 500));
    }

    @Test
    protected void transfer_from_savings_to_savings_should_be_greater_than_0() {
        String payingID = SAVINGS_ID_0;
        String receivingID = SAVINGS_ID_1;
        double transferAmount = 0;

        assertFalse(bank.isTransferAmountValid(payingID, receivingID, transferAmount - 500));
        assertFalse(bank.isTransferAmountValid(payingID, receivingID, transferAmount - 50));
        assertFalse(bank.isTransferAmountValid(payingID, receivingID, transferAmount));
        assertTrue(bank.isTransferAmountValid(payingID, receivingID, transferAmount + 50));
        assertTrue(bank.isTransferAmountValid(payingID, receivingID, 500));
    }

    @Test
    protected void transfer_from_savings_to_savings_should_be_less_than_or_equal_to_1000() {
        String payingID = SAVINGS_ID_0;
        String receivingID = SAVINGS_ID_1;
        double transferAmount = 1000;

        assertTrue(bank.isTransferAmountValid(payingID, receivingID, 600));
        assertTrue(bank.isTransferAmountValid(payingID, receivingID, transferAmount - 50));
        assertTrue(bank.isTransferAmountValid(payingID, receivingID, transferAmount));
        assertFalse(bank.isTransferAmountValid(payingID, receivingID, transferAmount + 50));
        assertFalse(bank.isTransferAmountValid(payingID, receivingID, transferAmount + 500));
    }

    @Test
    protected void transfer_to_cd_should_not_be_possible() {
        List<Double> transferAmounts = Arrays.asList(-500.0, -100.0, 0.0, 100.0, 1200.0, 1300.0, 2400.0, 2500.0, 2600.0, 3500.0);

        bank.timeTravel(getMonthsPerYear());

        for (Double transferAmount : transferAmounts) {
            assertFalse(bank.isTransferAmountValid(CHECKING_ID_1, CD_ID_0, transferAmount));
            assertFalse(bank.isTransferAmountValid(SAVINGS_ID_1, CD_ID_0, transferAmount));
            assertFalse(bank.isTransferAmountValid(CD_ID_1, CD_ID_0, transferAmount));
        }
    }

    @Test
    protected void transfer_from_cd_to_savings_after_12_month_inclusive() {
        int monthsPerYear = getMonthsPerYear();
        double transferAmount = min(bank.getAccount(CD_ID_1).getMaxWithdrawAmount(), bank.getAccount(SAVINGS_ID_1).getMaxDepositAmount());
        bank.deposit(SAVINGS_ID_0, transferAmount);

        for (int month = 0; month < monthsPerYear * 2; month++) {
            assertEquals(month >= monthsPerYear, bank.isTransferAmountValid(CD_ID_0, SAVINGS_ID_0, transferAmount));

            bank.timeTravel(1);
        }
    }

    @Test
    protected void transfer_from_cd_to_savings_should_be_between_balance_and_2500_inclusive() {
        double cdAPR = 0.6;
        initialCDBalance = 2200;

        List<Integer> months = Arrays.asList(getMonthsPerYear(), bank.getMaxMonths());
        String payingID = CD_ID_1;
        String receivingID = SAVINGS_ID_1;
        List<Double> lowerBound = new ArrayList<>();
        double upperBound = 2500;

        bank.removeAccount(payingID);
        bank.createCD(payingID, cdAPR, initialCDBalance);
        bank.deposit(receivingID, bank.getAccount(SAVINGS_ID_1).getMaxDepositAmount());
        lowerBound.add(passTime(bank.getMinBalanceFee(), months.get(0), initialCDBalance));
        lowerBound.add(passTime(bank.getMinBalanceFee(), months.get(1), lowerBound.get(0)));

        for (int i = 0; i < 2; i++) {
            bank.timeTravel(months.get(i));

            assertEquals(lowerBound.get(i), bank.getAccount(payingID).getBalance());
            assertFalse(bank.isTransferAmountValid(payingID, receivingID, lowerBound.get(i) - 500));
            assertFalse(bank.isTransferAmountValid(payingID, receivingID, lowerBound.get(i) - 50));
            assertEquals(lowerBound.get(i) <= upperBound, bank.isTransferAmountValid(payingID, receivingID, lowerBound.get(i)));
            assertEquals(lowerBound.get(i) <= upperBound, bank.isTransferAmountValid(payingID, receivingID, lowerBound.get(i) + 50));

            assertEquals(lowerBound.get(i) <= upperBound, bank.isTransferAmountValid(payingID, receivingID, upperBound - 100));
            assertEquals(lowerBound.get(i) <= upperBound, bank.isTransferAmountValid(payingID, receivingID, upperBound));
            assertFalse(bank.isTransferAmountValid(payingID, receivingID, upperBound + 100));
            assertFalse(bank.isTransferAmountValid(payingID, receivingID, upperBound + 1000));
        }
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
