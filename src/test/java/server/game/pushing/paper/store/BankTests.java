package server.game.pushing.paper.store;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.game.pushing.paper.store.bank.AccountType;
import server.game.pushing.paper.store.bank.Bank;
import server.game.pushing.paper.store.bank.account.Account;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.Math.max;
import static java.lang.Math.min;
import static org.junit.jupiter.api.Assertions.*;
import static server.game.pushing.paper.store.bank.AccountType.Savings;
import static server.game.pushing.paper.store.bank.Bank.getMonthsPerYear;

public class BankTests {
    private Bank bank;

    private final String checkingID0 = "34792497";
    private final String checkingID1 = "42793478";
    private final String savingsID0 = "00000000";
    private final String savingsID1 = "98340842";
    private final String cdID0 = "54873935";
    private final String cdID1 = "37599823";
    private final double cdBalance = 1475;

    public static double timeTravel(double balance, int months) {
        if (balance <= 900) {
            return max(0, balance - 100 * months);
        }

        return balance;
    }

    @BeforeEach
    protected void setUp() {
        bank = new Bank();

        bank.createCheckingAccount(checkingID0);
        bank.createCheckingAccount(checkingID1);
        bank.createSavingsAccount(savingsID1);
        bank.createSavingsAccount(savingsID0);
        bank.createCDAccount(cdID1, cdBalance);
        bank.createCDAccount(cdID0, cdBalance);
    }

    @Test
    protected void banks_can_create_accounts() {
        assertFalse(bank.isEmpty());
        assertEquals(6, bank.size());
        for (String id : bank.getAccounts()) {
            assertTrue(bank.containsAccount(id));
        }
    }

    @Test
    protected void an_account_has_an_account_type_like_checking_savings_or_cd_as_well_as_an_id_and_balance() {
        AccountType accountType = Savings;
        String id = savingsID1;
        double balance = 0;

        Account account = bank.getAccount(id);
        assertEquals(accountType, account.getAccountType());
        assertEquals(id, account.getID());
        assertEquals(balance, account.getBalance());
        assertEquals(String.format("%s %s %.2f", accountType, id, balance).toLowerCase(), account.toString());
    }

    @Test
    protected void a_bank_is_a_list_of_accounts() {
        bank = new Bank();

        assertTrue(bank.isEmpty());
        assertEquals(0, bank.size());
    }

    @Test
    protected void during_account_creation_banks_should_use_an_unique_8_digit_id() {
        assertFalse(bank.isIDValid(checkingID0));

        assertFalse(bank.isIDValid(""));
        assertFalse(bank.isIDValid("1"));
        assertFalse(bank.isIDValid("1234567"));
        assertFalse(bank.isIDValid("123456789"));

        assertFalse(bank.isIDValid("fireball"));

        assertTrue(bank.isIDValid("78349722"));
    }

    @Test
    protected void during_cd_creation_banks_should_use_a_balance_between_1000_and_10000_inclusive() {
        double cdBalance = bank.getMinCDBalance();
        assertEquals(1000, cdBalance);
        assertFalse(bank.isCDBalanceValid(cdBalance - 5000));
        assertFalse(bank.isCDBalanceValid(cdBalance - 1));
        assertTrue(bank.isCDBalanceValid(cdBalance));
        assertTrue(bank.isCDBalanceValid(cdBalance + 1));
        assertTrue(bank.isCDBalanceValid(5000));

        cdBalance = bank.getMaxCDBalance();
        assertEquals(10000, cdBalance);
        assertTrue(bank.isCDBalanceValid(6000));
        assertTrue(bank.isCDBalanceValid(cdBalance - 1));
        assertTrue(bank.isCDBalanceValid(cdBalance));
        assertFalse(bank.isCDBalanceValid(cdBalance + 1));
        assertFalse(bank.isCDBalanceValid(cdBalance + 5000));

        assertFalse(bank.isCDBalanceValid(Double.NEGATIVE_INFINITY));
        assertFalse(bank.isCDBalanceValid(0));
        assertFalse(bank.isCDBalanceValid(1));
        assertFalse(bank.isCDBalanceValid(Double.POSITIVE_INFINITY));
    }

    @Test
    protected void banks_can_time_travel_between_0_and_60_months_excluding_0() {
        assertFalse(bank.isTimeTravelValid(-10));
        assertFalse(bank.isTimeTravelValid(0));
        assertTrue(bank.isTimeTravelValid(1));
        assertTrue(bank.isTimeTravelValid(2));
        assertTrue(bank.isTimeTravelValid(30));

        assertTrue(bank.isTimeTravelValid(40));
        assertTrue(bank.isTimeTravelValid(50));
        assertTrue(bank.isTimeTravelValid(60));
        assertEquals(60, bank.getMaxTimeTravel());
        assertFalse(bank.isTimeTravelValid(70));
        assertFalse(bank.isTimeTravelValid(100));
    }

    @Test
    protected void the_min_balance_fee_is_100() {
        assertEquals(100, bank.getMinBalanceFee());
    }

    @Test
    protected void a_low_balance_account_has_a_balance_less_than_or_equal_to_900() {
        List<String> accounts = new ArrayList<>(Arrays.asList(checkingID0, savingsID1, savingsID0));
        List<Double> depositAmounts = new ArrayList<>(Arrays.asList(899., 900., 901.));
        for (int i = 0; i < accounts.size(); i++) {
            String id = accounts.get(i);
            double depositAmount = depositAmounts.get(i);

            bank.deposit(id, depositAmount);

            assertEquals(bank.getAccount(id).getBalance() <= 900, bank.isLowBalanceAccount(id));
        }
    }

    @Test
    protected void during_time_travel_the_bank_will_withdraw_the_min_balance_fee_from_low_balance_accounts() {
        List<String> accounts = new ArrayList<>(Arrays.asList(checkingID0, savingsID1, savingsID0));
        int months = 3;
        List<Double> depositAmounts = new ArrayList<>(Arrays.asList(800., 900., 1000.));
        for (int i = 0; i < accounts.size(); i++) {
            String id = accounts.get(i);
            double depositAmount = depositAmounts.get(i);

            bank.deposit(id, depositAmount);
            bank.timeTravel(months);

            assertEquals(timeTravel(depositAmount, months), bank.getAccount(id).getBalance());
        }
    }

    @Test
    protected void banks_can_deposit_to_checking_and_savings() {
        List<String> accounts = new ArrayList<>(Arrays.asList(checkingID0, savingsID1));
        for (String id : accounts) {
            double depositAmount = bank.getAccount(id).getMaxDepositAmount();

            bank.deposit(id, depositAmount);

            assertEquals(depositAmount, bank.getAccount(id).getBalance());
        }
    }

    @Test
    protected void banks_should_not_deposit_to_cd() {
        List<Double> depositAmounts = new ArrayList<>(Arrays.asList(-500., -1., 0., 1., 500., 600., 999., 1000., 1001., 1500.));

        for (Double depositAmount : depositAmounts) {
            assertFalse(bank.isDepositAmountValid(cdID0, depositAmount));
        }
    }

    @Test
    protected void during_deposits_banks_should_use_a_taken_id() {
        String id = savingsID1;
        double depositAmount = bank.getAccount(id).getMaxDepositAmount();

        assertFalse(bank.isDepositAmountValid("08243478", depositAmount));
        assertTrue(bank.isDepositAmountValid(id, depositAmount));
    }

    @Test
    protected void during_deposits_to_checking_banks_should_use_a_deposit_amount_between_0_and_1000_excluding_0() {
        String id = checkingID1;

        double depositAmount = 0;
        assertFalse(bank.isDepositAmountValid(id, depositAmount - 500));
        assertFalse(bank.isDepositAmountValid(id, depositAmount - 1));
        assertFalse(bank.isDepositAmountValid(id, depositAmount));
        assertTrue(bank.isDepositAmountValid(id, depositAmount + 1));
        assertTrue(bank.isDepositAmountValid(id, 500));

        depositAmount = bank.getAccount(id).getMaxDepositAmount();
        assertEquals(1000, depositAmount);
        assertTrue(bank.isDepositAmountValid(id, 600));
        assertTrue(bank.isDepositAmountValid(id, depositAmount - 1));
        assertTrue(bank.isDepositAmountValid(id, depositAmount));
        assertFalse(bank.isDepositAmountValid(id, depositAmount + 1));
        assertFalse(bank.isDepositAmountValid(id, depositAmount + 500));
    }

    @Test
    protected void during_deposits_to_savings_banks_should_use_a_deposit_amount_between_0_and_2500_excluding_0() {
        String id = savingsID1;

        double depositAmount = 0;
        assertFalse(bank.isDepositAmountValid(id, depositAmount - 1200));
        assertFalse(bank.isDepositAmountValid(id, depositAmount - 1));
        assertFalse(bank.isDepositAmountValid(id, depositAmount));
        assertTrue(bank.isDepositAmountValid(id, depositAmount + 1));
        assertTrue(bank.isDepositAmountValid(id, 1200));

        depositAmount = bank.getAccount(id).getMaxDepositAmount();
        assertEquals(2500, depositAmount);
        assertTrue(bank.isDepositAmountValid(id, 1300));
        assertTrue(bank.isDepositAmountValid(id, depositAmount - 1));
        assertTrue(bank.isDepositAmountValid(id, depositAmount));
        assertFalse(bank.isDepositAmountValid(id, depositAmount + 1));
        assertFalse(bank.isDepositAmountValid(id, depositAmount + 1200));
    }

    @Test
    protected void banks_can_withdraw_from_accounts() {
        List<String> accounts = new ArrayList<>(Arrays.asList(checkingID0, savingsID1, cdID1));
        for (String id : accounts) {
            double depositAmount = bank.getAccount(id).getMaxDepositAmount();
            double withdrawAmount = bank.getAccount(id).getMaxWithdrawAmount();

            bank.deposit(id, depositAmount);
            bank.withdraw(id, withdrawAmount);

            assertEquals(max(0, depositAmount - withdrawAmount), bank.getAccount(id).getBalance());
        }
    }

    @Test
    protected void if_the_withdraw_amount_is_greater_than_the_balance_then_the_bank_will_withdraw_the_balance_instead() {
        String id = savingsID1;
        double depositAmount = bank.getAccount(id).getMaxWithdrawAmount() / 2;
        double withdrawAmount = bank.getAccount(id).getMaxWithdrawAmount();

        bank.deposit(id, depositAmount);
        assertTrue(withdrawAmount > bank.getAccount(id).getBalance());

        bank.withdraw(id, withdrawAmount);
        assertEquals(0, bank.getAccount(id).getBalance());
    }

    @Test
    protected void during_withdraws_banks_should_use_a_taken_id() {
        String id = checkingID1;
        double withdrawAmount = bank.getAccount(id).getMaxWithdrawAmount();

        assertFalse(bank.isWithdrawAmountValid("34784792", withdrawAmount));
        assertTrue(bank.isWithdrawAmountValid(id, withdrawAmount));
    }

    @Test
    protected void during_withdraws_from_checking_banks_should_use_a_withdraw_amount_between_0_and_400_excluding_0() {
        String id = checkingID1;

        double withdrawAmount = 0;
        assertFalse(bank.isWithdrawAmountValid(id, withdrawAmount - 200));
        assertFalse(bank.isWithdrawAmountValid(id, withdrawAmount - 1));
        assertFalse(bank.isWithdrawAmountValid(id, withdrawAmount));
        assertTrue(bank.isWithdrawAmountValid(id, withdrawAmount + 1));
        assertTrue(bank.isWithdrawAmountValid(id, 200));

        withdrawAmount = bank.getAccount(id).getMaxWithdrawAmount();
        assertEquals(400, withdrawAmount);
        assertTrue(bank.isWithdrawAmountValid(id, 300));
        assertTrue(bank.isWithdrawAmountValid(id, withdrawAmount - 1));
        assertTrue(bank.isWithdrawAmountValid(id, withdrawAmount));
        assertFalse(bank.isWithdrawAmountValid(id, withdrawAmount + 1));
        assertFalse(bank.isWithdrawAmountValid(id, withdrawAmount + 200));
    }

    @Test
    protected void during_withdraws_from_savings_banks_should_use_a_withdraw_amount_between_0_and_1000_excluding_0() {
        String id = savingsID1;

        double withdrawAmount = 0;
        assertFalse(bank.isWithdrawAmountValid(id, withdrawAmount - 500));
        assertFalse(bank.isWithdrawAmountValid(id, withdrawAmount - 1));
        assertFalse(bank.isWithdrawAmountValid(id, withdrawAmount));
        assertTrue(bank.isWithdrawAmountValid(id, withdrawAmount + 1));
        assertTrue(bank.isWithdrawAmountValid(id, 500));

        withdrawAmount = bank.getAccount(id).getMaxWithdrawAmount();
        assertEquals(1000, withdrawAmount);
        assertTrue(bank.isWithdrawAmountValid(id, 600));
        assertTrue(bank.isWithdrawAmountValid(id, withdrawAmount - 1));
        assertTrue(bank.isWithdrawAmountValid(id, withdrawAmount));
        assertFalse(bank.isWithdrawAmountValid(id, withdrawAmount + 1));
        assertFalse(bank.isWithdrawAmountValid(id, withdrawAmount + 500));
    }

    @Test
    protected void during_withdraws_from_cd_banks_should_use_a_withdraw_amount_greater_than_or_equal_to_the_balance() {
        String id = cdID0;
        int months = getMonthsPerYear();
        double withdrawAmount = cdBalance;

        bank.timeTravel(months);
        assertEquals(withdrawAmount, bank.getAccount(id).getBalance());
        assertFalse(bank.isWithdrawAmountValid(id, withdrawAmount - 1000));
        assertFalse(bank.isWithdrawAmountValid(id, withdrawAmount - 100));
        assertTrue(bank.isWithdrawAmountValid(id, withdrawAmount));
        assertTrue(bank.isWithdrawAmountValid(id, withdrawAmount + 100));
        assertTrue(bank.isWithdrawAmountValid(id, withdrawAmount + 1000));

        assertFalse(bank.isWithdrawAmountValid(id, Double.NEGATIVE_INFINITY));
        assertFalse(bank.isWithdrawAmountValid(id, 0));
        assertFalse(bank.isWithdrawAmountValid(id, 1));
        assertTrue(bank.isWithdrawAmountValid(id, Double.POSITIVE_INFINITY));
    }

    @Test
    protected void banks_can_only_withdraw_from_a_savings_once_a_month() {
        String id = savingsID1;
        int months = 1;
        double withdrawAmount = bank.getAccount(id).getMaxWithdrawAmount();

        bank.withdraw(id, withdrawAmount);
        assertFalse(bank.isWithdrawAmountValid(id, withdrawAmount));

        bank.timeTravel(months);
        assertTrue(bank.isWithdrawAmountValid(id, withdrawAmount));
    }

    @Test
    protected void banks_can_only_withdraw_from_a_cd_after_a_year() {
        String id = cdID1;
        double withdrawAmount = bank.getAccount(id).getMaxWithdrawAmount();

        for (int months = 0; months < 24; months++) {
            assertEquals(months >= getMonthsPerYear(), bank.isWithdrawAmountValid(id, withdrawAmount));
            bank.timeTravel(1);
        }
    }

    @Test
    protected void banks_can_transfer() {
        String payingID = checkingID0;
        String receivingID = savingsID1;
        double transferAmount = min(bank.getAccount(payingID).getMaxWithdrawAmount(), bank.getAccount(receivingID).getMaxDepositAmount());

        bank.deposit(payingID, transferAmount);
        bank.transfer(payingID, receivingID, transferAmount);

        assertEquals(0, bank.getAccount(payingID).getBalance());
        assertEquals(transferAmount, bank.getAccount(receivingID).getBalance());
    }

    @Test
    protected void a_transfer_is_a_deposit_and_a_withdraw() {
        int months = getMonthsPerYear();
        String payingID = cdID0;
        String receivingID = savingsID0;
        double transferAmount = cdBalance;

        bank.timeTravel(months);

        assertTrue(bank.isWithdrawAmountValid(payingID, transferAmount));
        assertTrue(bank.isDepositAmountValid(receivingID, transferAmount));
        assertTrue(bank.isTransferAmountValid(payingID, receivingID, transferAmount));

        payingID = "87439742";
        transferAmount = bank.getMaxCDBalance();

        bank.createCDAccount(payingID, transferAmount);
        bank.timeTravel(months);

        assertTrue(bank.isWithdrawAmountValid(payingID, transferAmount));
        assertFalse(bank.isDepositAmountValid(receivingID, transferAmount));
        assertFalse(bank.isTransferAmountValid(payingID, receivingID, transferAmount));
    }

    @Test
    protected void if_the_transfer_amount_is_greater_than_the_paying_account_balance_then_the_bank_will_transfer_the_paying_account_balance_instead() {
        String payingID = checkingID0;
        String receivingID = savingsID1;
        double depositAmount = min(bank.getAccount(payingID).getMaxWithdrawAmount(), bank.getAccount(receivingID).getMaxDepositAmount());
        double transferAmount = Double.POSITIVE_INFINITY;

        bank.deposit(payingID, depositAmount);
        bank.transfer(payingID, receivingID, transferAmount);

        assertEquals(0, bank.getAccount(payingID).getBalance());
        assertEquals(depositAmount, bank.getAccount(receivingID).getBalance());
    }

    @Test
    protected void during_transfers_banks_should_use_a_different_paying_id_and_receiving_id() {
        String payingID = checkingID0;
        String receivingID = savingsID1;
        double transferAmount = min(bank.getAccount(payingID).getMaxWithdrawAmount(), bank.getAccount(receivingID).getMaxDepositAmount());

        assertFalse(bank.isTransferAmountValid(payingID, payingID, transferAmount));
        assertTrue(bank.isTransferAmountValid(payingID, receivingID, transferAmount));
    }
}
