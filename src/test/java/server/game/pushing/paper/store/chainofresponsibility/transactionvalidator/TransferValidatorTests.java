package server.game.pushing.paper.store.chainofresponsibility.transactionvalidator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.game.pushing.paper.store.bank.Bank;
import server.game.pushing.paper.store.bank.account.AccountType;
import server.game.pushing.paper.store.chainofresponsibility.ChainOfResponsibility;
import server.game.pushing.paper.store.chainofresponsibility.TransactionType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.Math.min;
import static org.junit.jupiter.api.Assertions.*;
import static server.game.pushing.paper.store.bank.Bank.getMonthsPerYear;
import static server.game.pushing.paper.store.bank.BankTests.passTime;

public class TransferValidatorTests {
    private Bank bank;
    private TransferValidator transferValidator;

    private final String CHECKING_ID_0 = "87439753";
    private final String CHECKING_ID_1 = "87439742";
    private final String SAVINGS_ID_0 = "98430842";
    private final String SAVINGS_ID_1 = "98430854";
    private final String CD_ID_0 = "24799348";
    private final String CD_ID_1 = "14799348";
    private double apr;
    private double initialCDBalance;

    @BeforeEach
    protected void setUp() {
        bank = new Bank();
        transferValidator = new TransferValidator(bank);

        apr = bank.getMaxAPR();
        initialCDBalance = bank.getMinInitialCDBalance();

        bank.createChecking(CHECKING_ID_0, apr);
        bank.createChecking(CHECKING_ID_1, apr);
        bank.createSavings(SAVINGS_ID_0, apr);
        bank.createSavings(SAVINGS_ID_1, apr);
        bank.createCD(CD_ID_0, apr, initialCDBalance);
        bank.createCD(CD_ID_1, apr, initialCDBalance);
        bank.deposit(CHECKING_ID_0, bank.getAccount(CHECKING_ID_1).getMaxWithdrawAmount());
        bank.deposit(CHECKING_ID_1, bank.getAccount(CHECKING_ID_1).getMaxDepositAmount());
        bank.deposit(SAVINGS_ID_0, bank.getAccount(SAVINGS_ID_1).getMaxDepositAmount());
        bank.deposit(SAVINGS_ID_1, bank.getAccount(SAVINGS_ID_1).getMaxDepositAmount());
    }

    @Test
    protected void transfer_validator_when_transaction_is_not_valid_should_pass_transaction_up_the_chain_of_responsibility() {
        transferValidator = (TransferValidator) ChainOfResponsibility.getInstance(Arrays.asList(transferValidator, new PassTimeValidator(bank), null));

        int months = getMonthsPerYear();
        AccountType accountType = AccountType.Savings;
        String id = "97420734";

        assertTrue(transferValidator.handle(String.format("%s %s", TransactionType.PassTime, months)));
        assertFalse(transferValidator.handle(String.format("%s %s %s %s", TransactionType.Create, accountType, id, apr)));
    }

    @Test
    protected void transaction_should_contain_the_transaction_type_transfer_as_the_first_argument() {
        TransactionType transactionType = TransactionType.Transfer;
        String fromID = CHECKING_ID_1;
        String toID = CHECKING_ID_0;
        double transferAmount = min(bank.getAccount(fromID).getMaxWithdrawAmount(), bank.getAccount(toID).getMaxDepositAmount());

        assertFalse(transferValidator.handle(String.format("%s %s %s %s", "", "", "", "")));
        assertFalse(transferValidator.handle(String.format("%s %s %s %s", "", fromID, toID, transferAmount)));
        assertFalse(transferValidator.handle(String.format("%s %s %s %s", "nuke", fromID, toID, transferAmount)));
        assertTrue(transferValidator.handle(String.format("%s %s %s %s", transactionType, fromID, toID, transferAmount)));
    }

    @Test
    protected void transaction_should_contain_a_unique_and_taken_from_and_to_id_as_the_second_and_third_argument() {
        TransactionType transactionType = TransactionType.Transfer;
        String fromID = SAVINGS_ID_1;
        String toID = SAVINGS_ID_0;
        double transferAmount = min(bank.getAccount(fromID).getMaxWithdrawAmount(), bank.getAccount(toID).getMaxDepositAmount());

        assertFalse(transferValidator.handle(String.format("%s %s %s %s", transactionType, "", "", "")));
        assertFalse(transferValidator.handle(String.format("%s %s %s %s", transactionType, "", "", transactionType)));
        assertFalse(transferValidator.handle(String.format("%s %s %s %s", transactionType, "", toID, transferAmount)));
        assertFalse(transferValidator.handle(String.format("%s %s %s %s", transactionType, fromID, "", "")));
        assertFalse(transferValidator.handle(String.format("%s %s %s %s", transactionType, fromID, "", transactionType)));

        assertFalse(transferValidator.handle(String.format("%s %s %s %s", transactionType, fromID, fromID, transferAmount)));
        assertFalse(transferValidator.handle(String.format("%s %s %s %s", transactionType, "34782792", toID, transferAmount)));
        assertFalse(transferValidator.handle(String.format("%s %s %s %s", transactionType, fromID, "78344278", transferAmount)));
        assertTrue(transferValidator.handle(String.format("%s %s %s %s", transactionType, fromID, toID, transferAmount)));
    }

    @Test
    protected void transaction_should_contain_a_transfer_amount_as_the_fourth_argument() {
        TransactionType transactionType = TransactionType.Transfer;
        String fromID = CD_ID_0;
        String toID = SAVINGS_ID_0;
        double transferAmount = min(bank.getAccount(fromID).getMaxWithdrawAmount(), bank.getAccount(toID).getMaxDepositAmount());

        bank.passTime(getMonthsPerYear());

        assertFalse(transferValidator.handle(String.format("%s %s %s %s", transactionType, fromID, toID, "")));
        assertFalse(transferValidator.handle(String.format("%s %s %s %s", transactionType, fromID, toID, "^*(FGd")));
        assertTrue(transferValidator.handle(String.format("%s %s %s %s", transactionType, fromID, toID, transferAmount)));
    }

    @Test
    protected void transaction_when_account_type_is_from_checking_to_checking_should_contain_a_transfer_amount_greater_than_0() {
        TransactionType transactionType = TransactionType.Transfer;
        String fromID = CHECKING_ID_0;
        String toID = CHECKING_ID_1;
        double transferAmount = 0;

        assertFalse(transferValidator.handle(String.format("%s %s %s %s", transactionType, fromID, toID, transferAmount - 300)));
        assertFalse(transferValidator.handle(String.format("%s %s %s %s", transactionType, fromID, toID, transferAmount - 3)));
        assertFalse(transferValidator.handle(String.format("%s %s %s %s", transactionType, fromID, toID, transferAmount)));
        assertTrue(transferValidator.handle(String.format("%s %s %s %s", transactionType, fromID, toID, transferAmount + 3)));
    }

    @Test
    protected void transaction_when_account_type_is_from_checking_to_checking_should_contain_a_transfer_amount_less_than_or_equal_to_400() {
        TransactionType transactionType = TransactionType.Transfer;
        String fromID = CHECKING_ID_1;
        String toID = CHECKING_ID_0;
        double transferAmount = 400;

        assertTrue(transferValidator.handle(String.format("%s %s %s %s", transactionType, fromID, toID, transferAmount - 3)));
        assertTrue(transferValidator.handle(String.format("%s %s %s %s", transactionType, fromID, toID, transferAmount)));
        assertFalse(transferValidator.handle(String.format("%s %s %s %s", transactionType, fromID, toID, transferAmount + 3)));
        assertFalse(transferValidator.handle(String.format("%s %s %s %s", transactionType, fromID, toID, transferAmount + 300)));
    }

    @Test
    protected void transaction_when_account_type_is_from_checking_to_savings_should_contain_a_transfer_amount_greater_than_0() {
        TransactionType transactionType = TransactionType.Transfer;
        String fromID = CHECKING_ID_0;
        String toID = SAVINGS_ID_0;
        double transferAmount = 0;

        assertFalse(transferValidator.handle(String.format("%s %s %s %s", transactionType, fromID, toID, transferAmount - 300)));
        assertFalse(transferValidator.handle(String.format("%s %s %s %s", transactionType, fromID, toID, transferAmount - 3)));
        assertFalse(transferValidator.handle(String.format("%s %s %s %s", transactionType, fromID, toID, transferAmount)));
        assertTrue(transferValidator.handle(String.format("%s %s %s %s", transactionType, fromID, toID, transferAmount + 3)));
    }

    @Test
    protected void transaction_when_account_type_is_from_checking_to_savings_should_contain_a_transfer_amount_less_than_or_equal_to_400() {
        TransactionType transactionType = TransactionType.Transfer;
        String fromID = CHECKING_ID_1;
        String toID = SAVINGS_ID_1;
        double transferAmount = 400;

        assertTrue(transferValidator.handle(String.format("%s %s %s %s", transactionType, fromID, toID, transferAmount - 3)));
        assertTrue(transferValidator.handle(String.format("%s %s %s %s", transactionType, fromID, toID, transferAmount)));
        assertFalse(transferValidator.handle(String.format("%s %s %s %s", transactionType, fromID, toID, transferAmount + 3)));
        assertFalse(transferValidator.handle(String.format("%s %s %s %s", transactionType, fromID, toID, transferAmount + 300)));
    }

    @Test
    protected void transaction_when_account_type_is_from_savings_should_not_be_possible_twice_a_month_or_more() {
        String fromID = SAVINGS_ID_1;
        String toID = CHECKING_ID_1;
        double transferAmount = min(bank.getAccount(fromID).getMaxWithdrawAmount(), bank.getAccount(toID).getMaxDepositAmount());
        String transaction = String.format("%s %s %s %s", TransactionType.Transfer, fromID, toID, transferAmount);

        assertTrue(transferValidator.handle(transaction));
        bank.transfer(fromID, toID, transferAmount);

        assertFalse(transferValidator.handle(transaction));

        bank.passTime(1);
        assertTrue(transferValidator.handle(transaction));
    }

    @Test
    protected void transaction_when_account_type_is_from_savings_to_checking_should_contain_a_transfer_amount_greater_than_0() {
        TransactionType transactionType = TransactionType.Transfer;
        String fromID = SAVINGS_ID_0;
        String toID = CHECKING_ID_1;
        double transferAmount = 0;

        assertFalse(transferValidator.handle(String.format("%s %s %s %s", transactionType, fromID, toID, transferAmount - 300)));
        assertFalse(transferValidator.handle(String.format("%s %s %s %s", transactionType, fromID, toID, transferAmount - 3)));
        assertFalse(transferValidator.handle(String.format("%s %s %s %s", transactionType, fromID, toID, transferAmount)));
        assertTrue(transferValidator.handle(String.format("%s %s %s %s", transactionType, fromID, toID, transferAmount + 3)));
        assertTrue(transferValidator.handle(String.format("%s %s %s %s", transactionType, fromID, toID, 500)));
    }

    @Test
    protected void transaction_when_account_type_is_from_savings_to_checking_should_contain_a_transfer_amount_less_than_or_equal_to_1000() {
        TransactionType transactionType = TransactionType.Transfer;
        String fromID = SAVINGS_ID_1;
        String toID = CHECKING_ID_1;
        double transferAmount = 1000;

        assertTrue(transferValidator.handle(String.format("%s %s %s %s", transactionType, fromID, toID, 600)));
        assertTrue(transferValidator.handle(String.format("%s %s %s %s", transactionType, fromID, toID, transferAmount - 3)));
        assertTrue(transferValidator.handle(String.format("%s %s %s %s", transactionType, fromID, toID, transferAmount)));
        assertFalse(transferValidator.handle(String.format("%s %s %s %s", transactionType, fromID, toID, transferAmount + 3)));
        assertFalse(transferValidator.handle(String.format("%s %s %s %s", transactionType, fromID, toID, transferAmount + 300)));
    }

    @Test
    protected void transaction_when_account_type_is_from_savings_to_savings_should_contain_a_transfer_amount_greater_than_0() {
        TransactionType transactionType = TransactionType.Transfer;
        String fromID = SAVINGS_ID_0;
        String toID = SAVINGS_ID_1;
        double transferAmount = 0;

        assertFalse(transferValidator.handle(String.format("%s %s %s %s", transactionType, fromID, toID, transferAmount - 300)));
        assertFalse(transferValidator.handle(String.format("%s %s %s %s", transactionType, fromID, toID, transferAmount - 3)));
        assertFalse(transferValidator.handle(String.format("%s %s %s %s", transactionType, fromID, toID, transferAmount)));
        assertTrue(transferValidator.handle(String.format("%s %s %s %s", transactionType, fromID, toID, transferAmount + 3)));
        assertTrue(transferValidator.handle(String.format("%s %s %s %s", transactionType, fromID, toID, 500)));
    }

    @Test
    protected void transaction_when_account_type_is_from_savings_to_savings_should_contain_a_transfer_amount_less_than_or_equal_to_1000() {
        TransactionType transactionType = TransactionType.Transfer;
        String fromID = SAVINGS_ID_1;
        String toID = SAVINGS_ID_0;
        double transferAmount = 1000;

        assertTrue(transferValidator.handle(String.format("%s %s %s %s", transactionType, fromID, toID, 600)));
        assertTrue(transferValidator.handle(String.format("%s %s %s %s", transactionType, fromID, toID, transferAmount - 3)));
        assertTrue(transferValidator.handle(String.format("%s %s %s %s", transactionType, fromID, toID, transferAmount)));
        assertFalse(transferValidator.handle(String.format("%s %s %s %s", transactionType, fromID, toID, transferAmount + 3)));
        assertFalse(transferValidator.handle(String.format("%s %s %s %s", transactionType, fromID, toID, transferAmount + 300)));
    }

    @Test
    protected void transfer_to_cd_should_not_be_possible() {
        TransactionType transactionType = TransactionType.Transfer;
        String toID = CD_ID_1;
        List<Double> transferAmounts = Arrays.asList(-300.0, -3.0, 0.0, 3.0, 1200.0, 1300.0, 2497.0, 2500.0, 2503.0, 2800.0);

        bank.passTime(getMonthsPerYear());

        for (Double transferAmount : transferAmounts) {
            assertFalse(transferValidator.handle(String.format("%s %s %s %s", transactionType, CHECKING_ID_0, toID, transferAmount)));
            assertFalse(transferValidator.handle(String.format("%s %s %s %s", transactionType, SAVINGS_ID_1, toID, transferAmount)));
            assertFalse(transferValidator.handle(String.format("%s %s %s %s", transactionType, CD_ID_0, toID, transferAmount)));
        }
    }

    @Test
    protected void transfer_from_cd_to_savings_should_be_possible_after_12_month_inclusive() {
        int monthsPerYear = getMonthsPerYear();

        for (int month = 0; month < monthsPerYear * 2; month++) {
            assertEquals(month >= monthsPerYear, transferValidator.handle(String.format("%s %s %s %s", TransactionType.Transfer, CD_ID_0, SAVINGS_ID_0, bank.getAccount(SAVINGS_ID_1).getMaxDepositAmount())));

            bank.passTime(1);
        }
    }

    @Test
    protected void transfer_from_cd_to_savings_should_be_between_balance_and_2500_inclusive() {
        double cdAPR = 0.6;
        initialCDBalance = 2200;

        List<Integer> months = Arrays.asList(getMonthsPerYear(), 60);
        TransactionType transactionType = TransactionType.Transfer;
        String fromID = CD_ID_1;
        String toID = SAVINGS_ID_0;
        List<Double> lowerBound = new ArrayList<>();
        double upperBound = 2500;

        bank.removeAccount(fromID);
        bank.createCD(fromID, cdAPR, initialCDBalance);
        bank.deposit(toID, bank.getAccount(toID).getMaxDepositAmount());
        lowerBound.add(passTime(bank.getMinBalanceFee(), months.get(0), AccountType.CD, cdAPR, initialCDBalance));
        lowerBound.add(passTime(bank.getMinBalanceFee(), months.get(1), AccountType.CD, cdAPR, lowerBound.get(0)));

        for (int i = 0; i < 2; i++) {
            bank.passTime(months.get(i));

            assertEquals(lowerBound.get(i), bank.getAccount(fromID).getBalance());
            assertFalse(transferValidator.handle(String.format("%s %s %s %s", transactionType, fromID, toID, lowerBound.get(i) - 500)));
            assertFalse(transferValidator.handle(String.format("%s %s %s %s", transactionType, fromID, toID, lowerBound.get(i) - 50)));
            assertEquals(lowerBound.get(i) <= upperBound, transferValidator.handle(String.format("%s %s %s %s", transactionType, fromID, toID, lowerBound.get(i))));
            assertEquals(lowerBound.get(i) <= upperBound, transferValidator.handle(String.format("%s %s %s %s", transactionType, fromID, toID, lowerBound.get(i) + 50)));

            assertEquals(lowerBound.get(i) <= upperBound, transferValidator.handle(String.format("%s %s %s %s", transactionType, fromID, toID, upperBound - 100)));
            assertEquals(lowerBound.get(i) <= upperBound, transferValidator.handle(String.format("%s %s %s %s", transactionType, fromID, toID, upperBound)));
            assertFalse(transferValidator.handle(String.format("%s %s %s %s", transactionType, fromID, toID, upperBound + 100)));
            assertFalse(transferValidator.handle(String.format("%s %s %s %s", transactionType, fromID, toID, upperBound + 1000)));
        }
    }

    @Test
    protected void transaction_should_be_case_insensitive() {
        String fromID = CHECKING_ID_1;
        String toID = CHECKING_ID_0;
        double transferAmount = min(bank.getAccount(fromID).getMaxWithdrawAmount(), bank.getAccount(toID).getMaxDepositAmount());

        assertTrue(transferValidator.handle(String.format("traNSFer %s %s %s", fromID, toID, transferAmount)));
    }

    @Test
    protected void transaction_should_be_possible_with_useless_additional_arguments() {
        TransactionType transactionType = TransactionType.Transfer;
        String fromID = CD_ID_1;
        String toID = SAVINGS_ID_1;
        double transferAmount = min(bank.getAccount(fromID).getMaxWithdrawAmount(), bank.getAccount(toID).getMaxDepositAmount());

        bank.passTime(12);

        assertTrue(transferValidator.handle(String.format("%s %s %s %s %s", transactionType, fromID, toID, transferAmount, "nuke")));
        assertTrue(transferValidator.handle(String.format("%s %s %s %s  %s  %s %s  %s   %s", transactionType, fromID, toID, transferAmount, "DsDifJ", "paSJiOf", "ps3f&jf", "sp@&HR*&HDSoa", "psd)(Jo")));
    }
}
