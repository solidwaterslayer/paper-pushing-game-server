package server.game.pushing.paper.store.chain_of_responsibility.transaction_validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.game.pushing.paper.store.bank.Bank;
import server.game.pushing.paper.store.bank.account.AccountType;
import server.game.pushing.paper.store.chain_of_responsibility.ChainOfResponsibility;
import server.game.pushing.paper.store.chain_of_responsibility.TransactionType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.Math.min;
import static org.junit.jupiter.api.Assertions.*;
import static server.game.pushing.paper.store.bank.Bank.getMonthsPerYear;
import static server.game.pushing.paper.store.bank.BankTests.timeTravel;

public class TransferValidatorTests {
    private Bank bank;
    private ChainOfResponsibility validator;

    private final int MONTHS = getMonthsPerYear();
    private TransactionType transactionType;
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
        validator = new TransferValidator(bank);

        transactionType = validator.getTransactionType();
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
    protected void transfer_validator_when_transaction_is_not_valid_should_pass_transaction_down_the_chain_of_responsibility() {
        bank = new Bank();
        validator = new TransferValidator(bank);

        AccountType accountType = AccountType.SAVINGS;

        validator.setNext(new PassTimeValidator(bank));

        assertTrue(validator.handle(String.format("%s %s", TransactionType.PassTime, MONTHS)));
        assertFalse(validator.handle(String.format("%s %s %s %s", TransactionType.Create, accountType, SAVINGS_ID_1, apr)));
    }

    @Test
    protected void transaction_should_contain_the_transaction_type_transfer_as_the_first_argument() {
        String payingID = CHECKING_ID_1;
        String receivingID = CHECKING_ID_0;
        double transferAmount = min(bank.getAccount(payingID).getMaxWithdrawAmount(), bank.getAccount(receivingID).getMaxDepositAmount());

        assertFalse(validator.handle(""));
        assertFalse(validator.handle(String.format("%s %s %s %s", "", "", "", "")));
        assertFalse(validator.handle(String.format("%s %s %s %s", "", payingID, receivingID, transferAmount)));
        assertFalse(validator.handle(String.format("%s %s %s %s", "nuke", payingID, receivingID, transferAmount)));
        assertTrue(validator.handle(String.format("%s %s %s %s", transactionType, payingID, receivingID, transferAmount)));
    }

    @Test
    protected void transaction_should_contain_a_unique_and_taken_from_and_to_id_as_the_second_and_third_argument() {
        String payingID = SAVINGS_ID_1;
        String receivingID = SAVINGS_ID_0;
        double transferAmount = min(bank.getAccount(payingID).getMaxWithdrawAmount(), bank.getAccount(receivingID).getMaxDepositAmount());

        assertFalse(validator.handle(String.format("%s %s %s %s", transactionType, "", "", "")));
        assertFalse(validator.handle(String.format("%s %s %s %s", transactionType, "", "", transactionType)));
        assertFalse(validator.handle(String.format("%s %s %s %s", transactionType, "", receivingID, transferAmount)));
        assertFalse(validator.handle(String.format("%s %s %s %s", transactionType, payingID, "", "")));
        assertFalse(validator.handle(String.format("%s %s %s %s", transactionType, payingID, "", transactionType)));

        assertFalse(validator.handle(String.format("%s %s %s %s", transactionType, payingID, payingID, transferAmount)));
        assertFalse(validator.handle(String.format("%s %s %s %s", transactionType, "34782792", receivingID, transferAmount)));
        assertFalse(validator.handle(String.format("%s %s %s %s", transactionType, payingID, "78344278", transferAmount)));
        assertTrue(validator.handle(String.format("%s %s %s %s", transactionType, payingID, receivingID, transferAmount)));
    }

    @Test
    protected void transaction_should_contain_a_transfer_amount_as_the_fourth_argument() {
        String payingID = CD_ID_0;
        String receivingID = SAVINGS_ID_0;
        double transferAmount = min(bank.getAccount(payingID).getMaxWithdrawAmount(), bank.getAccount(receivingID).getMaxDepositAmount());

        bank.timeTravel(getMonthsPerYear());

        assertFalse(validator.handle(String.format("%s %s %s %s", transactionType, payingID, receivingID, "")));
        assertFalse(validator.handle(String.format("%s %s %s %s", transactionType, payingID, receivingID, "^*(FGd")));
        assertTrue(validator.handle(String.format("%s %s %s %s", transactionType, payingID, receivingID, transferAmount)));
    }

    @Test
    protected void transaction_when_account_type_is_from_checking_to_checking_should_contain_a_transfer_amount_greater_than_0() {
        String payingID = CHECKING_ID_0;
        String receivingID = CHECKING_ID_1;
        double transferAmount = 0;

        assertFalse(validator.handle(String.format("%s %s %s %s", transactionType, payingID, receivingID, transferAmount - 300)));
        assertFalse(validator.handle(String.format("%s %s %s %s", transactionType, payingID, receivingID, transferAmount - 3)));
        assertFalse(validator.handle(String.format("%s %s %s %s", transactionType, payingID, receivingID, transferAmount)));
        assertTrue(validator.handle(String.format("%s %s %s %s", transactionType, payingID, receivingID, transferAmount + 3)));
    }

    @Test
    protected void transaction_when_account_type_is_from_checking_to_checking_should_contain_a_transfer_amount_less_than_or_equal_to_400() {
        String payingID = CHECKING_ID_1;
        String receivingID = CHECKING_ID_0;
        double transferAmount = 400;

        assertTrue(validator.handle(String.format("%s %s %s %s", transactionType, payingID, receivingID, transferAmount - 3)));
        assertTrue(validator.handle(String.format("%s %s %s %s", transactionType, payingID, receivingID, transferAmount)));
        assertFalse(validator.handle(String.format("%s %s %s %s", transactionType, payingID, receivingID, transferAmount + 3)));
        assertFalse(validator.handle(String.format("%s %s %s %s", transactionType, payingID, receivingID, transferAmount + 300)));
    }

    @Test
    protected void transaction_when_account_type_is_from_checking_to_savings_should_contain_a_transfer_amount_greater_than_0() {
        String payingID = CHECKING_ID_0;
        String receivingID = SAVINGS_ID_0;
        double transferAmount = 0;

        assertFalse(validator.handle(String.format("%s %s %s %s", transactionType, payingID, receivingID, transferAmount - 300)));
        assertFalse(validator.handle(String.format("%s %s %s %s", transactionType, payingID, receivingID, transferAmount - 3)));
        assertFalse(validator.handle(String.format("%s %s %s %s", transactionType, payingID, receivingID, transferAmount)));
        assertTrue(validator.handle(String.format("%s %s %s %s", transactionType, payingID, receivingID, transferAmount + 3)));
    }

    @Test
    protected void transaction_when_account_type_is_from_checking_to_savings_should_contain_a_transfer_amount_less_than_or_equal_to_400() {
        String payingID = CHECKING_ID_1;
        String receivingID = SAVINGS_ID_1;
        double transferAmount = 400;

        assertTrue(validator.handle(String.format("%s %s %s %s", transactionType, payingID, receivingID, transferAmount - 3)));
        assertTrue(validator.handle(String.format("%s %s %s %s", transactionType, payingID, receivingID, transferAmount)));
        assertFalse(validator.handle(String.format("%s %s %s %s", transactionType, payingID, receivingID, transferAmount + 3)));
        assertFalse(validator.handle(String.format("%s %s %s %s", transactionType, payingID, receivingID, transferAmount + 300)));
    }

    @Test
    protected void transaction_when_account_type_is_from_savings_should_not_be_possible_twice_a_month_or_more() {
        String payingID = SAVINGS_ID_1;
        String receivingID = CHECKING_ID_1;
        double transferAmount = min(bank.getAccount(payingID).getMaxWithdrawAmount(), bank.getAccount(receivingID).getMaxDepositAmount());
        String transaction = String.format("%s %s %s %s", TransactionType.Transfer, payingID, receivingID, transferAmount);

        assertTrue(validator.handle(transaction));
        bank.transfer(payingID, receivingID, transferAmount);

        assertFalse(validator.handle(transaction));

        bank.timeTravel(1);
        assertTrue(validator.handle(transaction));
    }

    @Test
    protected void transaction_when_account_type_is_from_savings_to_checking_should_contain_a_transfer_amount_greater_than_0() {
        String payingID = SAVINGS_ID_0;
        String receivingID = CHECKING_ID_1;
        double transferAmount = 0;

        assertFalse(validator.handle(String.format("%s %s %s %s", transactionType, payingID, receivingID, transferAmount - 300)));
        assertFalse(validator.handle(String.format("%s %s %s %s", transactionType, payingID, receivingID, transferAmount - 3)));
        assertFalse(validator.handle(String.format("%s %s %s %s", transactionType, payingID, receivingID, transferAmount)));
        assertTrue(validator.handle(String.format("%s %s %s %s", transactionType, payingID, receivingID, transferAmount + 3)));
        assertTrue(validator.handle(String.format("%s %s %s %s", transactionType, payingID, receivingID, 500)));
    }

    @Test
    protected void transaction_when_account_type_is_from_savings_to_checking_should_contain_a_transfer_amount_less_than_or_equal_to_1000() {
        String payingID = SAVINGS_ID_1;
        String receivingID = CHECKING_ID_1;
        double transferAmount = 1000;

        assertTrue(validator.handle(String.format("%s %s %s %s", transactionType, payingID, receivingID, 600)));
        assertTrue(validator.handle(String.format("%s %s %s %s", transactionType, payingID, receivingID, transferAmount - 3)));
        assertTrue(validator.handle(String.format("%s %s %s %s", transactionType, payingID, receivingID, transferAmount)));
        assertFalse(validator.handle(String.format("%s %s %s %s", transactionType, payingID, receivingID, transferAmount + 3)));
        assertFalse(validator.handle(String.format("%s %s %s %s", transactionType, payingID, receivingID, transferAmount + 300)));
    }

    @Test
    protected void transaction_when_account_type_is_from_savings_to_savings_should_contain_a_transfer_amount_greater_than_0() {
        String payingID = SAVINGS_ID_0;
        String receivingID = SAVINGS_ID_1;
        double transferAmount = 0;

        assertFalse(validator.handle(String.format("%s %s %s %s", transactionType, payingID, receivingID, transferAmount - 300)));
        assertFalse(validator.handle(String.format("%s %s %s %s", transactionType, payingID, receivingID, transferAmount - 3)));
        assertFalse(validator.handle(String.format("%s %s %s %s", transactionType, payingID, receivingID, transferAmount)));
        assertTrue(validator.handle(String.format("%s %s %s %s", transactionType, payingID, receivingID, transferAmount + 3)));
        assertTrue(validator.handle(String.format("%s %s %s %s", transactionType, payingID, receivingID, 500)));
    }

    @Test
    protected void transaction_when_account_type_is_from_savings_to_savings_should_contain_a_transfer_amount_less_than_or_equal_to_1000() {
        String payingID = SAVINGS_ID_1;
        String receivingID = SAVINGS_ID_0;
        double transferAmount = 1000;

        assertTrue(validator.handle(String.format("%s %s %s %s", transactionType, payingID, receivingID, 600)));
        assertTrue(validator.handle(String.format("%s %s %s %s", transactionType, payingID, receivingID, transferAmount - 3)));
        assertTrue(validator.handle(String.format("%s %s %s %s", transactionType, payingID, receivingID, transferAmount)));
        assertFalse(validator.handle(String.format("%s %s %s %s", transactionType, payingID, receivingID, transferAmount + 3)));
        assertFalse(validator.handle(String.format("%s %s %s %s", transactionType, payingID, receivingID, transferAmount + 300)));
    }

    @Test
    protected void transfer_to_cd_should_not_be_possible() {
        String receivingID = CD_ID_1;
        List<Double> transferAmounts = Arrays.asList(-300.0, -3.0, 0.0, 3.0, 1200.0, 1300.0, 2497.0, 2500.0, 2503.0, 2800.0);

        bank.timeTravel(MONTHS);

        for (Double transferAmount : transferAmounts) {
            assertFalse(validator.handle(String.format("%s %s %s %s", transactionType, CHECKING_ID_0, receivingID, transferAmount)));
            assertFalse(validator.handle(String.format("%s %s %s %s", transactionType, SAVINGS_ID_1, receivingID, transferAmount)));
            assertFalse(validator.handle(String.format("%s %s %s %s", transactionType, CD_ID_0, receivingID, transferAmount)));
        }
    }

    @Test
    protected void transfer_from_cd_to_savings_should_be_possible_after_12_month_inclusive() {
        int monthsPerYear = getMonthsPerYear();

        for (int month = 0; month < monthsPerYear * 2; month++) {
            assertEquals(month >= monthsPerYear, validator.handle(String.format("%s %s %s %s", TransactionType.Transfer, CD_ID_0, SAVINGS_ID_0, bank.getAccount(SAVINGS_ID_1).getMaxDepositAmount())));

            bank.timeTravel(1);
        }
    }

    @Test
    protected void transfer_from_cd_to_savings_should_be_between_balance_and_2500_inclusive() {
        double cdAPR = 0.6;
        initialCDBalance = 2200;

        List<Integer> months = Arrays.asList(getMonthsPerYear(), bank.getMaxMonths());
        String payingID = CD_ID_1;
        String receivingID = SAVINGS_ID_0;
        List<Double> lowerBound = new ArrayList<>();
        double upperBound = 2500;

        bank.removeAccount(payingID);
        bank.createCD(payingID, cdAPR, initialCDBalance);
        bank.deposit(receivingID, bank.getAccount(receivingID).getMaxDepositAmount());
        lowerBound.add(timeTravel(bank.getMinBalanceFee(), months.get(0), initialCDBalance));
        lowerBound.add(timeTravel(bank.getMinBalanceFee(), months.get(1), lowerBound.get(0)));

        for (int i = 0; i < 2; i++) {
            bank.timeTravel(months.get(i));

            assertEquals(lowerBound.get(i), bank.getAccount(payingID).getBalance());
            assertFalse(validator.handle(String.format("%s %s %s %s", transactionType, payingID, receivingID, lowerBound.get(i) - 500)));
            assertFalse(validator.handle(String.format("%s %s %s %s", transactionType, payingID, receivingID, lowerBound.get(i) - 50)));
            assertEquals(lowerBound.get(i) <= upperBound, validator.handle(String.format("%s %s %s %s", transactionType, payingID, receivingID, lowerBound.get(i))));
            assertEquals(lowerBound.get(i) <= upperBound, validator.handle(String.format("%s %s %s %s", transactionType, payingID, receivingID, lowerBound.get(i) + 50)));

            assertEquals(lowerBound.get(i) <= upperBound, validator.handle(String.format("%s %s %s %s", transactionType, payingID, receivingID, upperBound - 100)));
            assertEquals(lowerBound.get(i) <= upperBound, validator.handle(String.format("%s %s %s %s", transactionType, payingID, receivingID, upperBound)));
            assertFalse(validator.handle(String.format("%s %s %s %s", transactionType, payingID, receivingID, upperBound + 100)));
            assertFalse(validator.handle(String.format("%s %s %s %s", transactionType, payingID, receivingID, upperBound + 1000)));
        }
    }

    @Test
    protected void transaction_should_be_case_insensitive() {
        String payingID = CHECKING_ID_1;
        String receivingID = CHECKING_ID_0;
        double transferAmount = min(bank.getAccount(payingID).getMaxWithdrawAmount(), bank.getAccount(receivingID).getMaxDepositAmount());

        assertTrue(validator.handle(String.format("traNSFer %s %s %s", payingID, receivingID, transferAmount)));
    }

    @Test
    protected void transaction_should_be_possible_with_useless_additional_arguments() {
        String payingID = CD_ID_1;
        String receivingID = SAVINGS_ID_1;
        double transferAmount = min(bank.getAccount(payingID).getMaxWithdrawAmount(), bank.getAccount(receivingID).getMaxDepositAmount());

        bank.timeTravel(12);

        assertTrue(validator.handle(String.format("%s %s %s %s %s", transactionType, payingID, receivingID, transferAmount, "nuke")));
        assertTrue(validator.handle(String.format("%s %s %s %s  %s  %s %s  %s   %s", transactionType, payingID, receivingID, transferAmount, "DsDifJ", "paSJiOf", "ps3f&jf", "sp@&HR*&HDSoa", "psd)(Jo")));
    }
}
