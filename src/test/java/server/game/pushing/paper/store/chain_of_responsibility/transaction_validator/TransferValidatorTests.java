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
    private double cdBalance;

    @BeforeEach
    protected void setUp() {
        bank = new Bank();
        validator = new TransferValidator(bank);

        transactionType = validator.getTransactionType();
        cdBalance = bank.getMinCDBalance();

        bank.createCheckingAccount(CHECKING_ID_0);
        bank.createCheckingAccount(CHECKING_ID_1);
        bank.createSavingsAccount(SAVINGS_ID_0);
        bank.createSavingsAccount(SAVINGS_ID_1);
        bank.createCDAccount(CD_ID_0, cdBalance);
        bank.createCDAccount(CD_ID_1, cdBalance);
        bank.deposit(CHECKING_ID_0, bank.getAccount(CHECKING_ID_1).getMaxWithdrawAmount());
        bank.deposit(CHECKING_ID_1, bank.getAccount(CHECKING_ID_1).getMaxDepositAmount());
        bank.deposit(SAVINGS_ID_0, bank.getAccount(SAVINGS_ID_1).getMaxDepositAmount());
        bank.deposit(SAVINGS_ID_1, bank.getAccount(SAVINGS_ID_1).getMaxDepositAmount());
    }

    @Test
    protected void the_first_argument_of_transfer_transactions_is_the_transaction_type_transfer() {
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
    protected void the_second_and_third_argument_of_transfer_transactions_is_a_different_and_taken_paying_id_and_receiving_id() {
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
    protected void the_fourth_argument_of_transfer_transactions_is_a_transfer_amount() {
        String payingID = CD_ID_0;
        String receivingID = SAVINGS_ID_0;
        double transferAmount = min(bank.getAccount(payingID).getMaxWithdrawAmount(), bank.getAccount(receivingID).getMaxDepositAmount());

        bank.timeTravel(getMonthsPerYear());

        assertFalse(validator.handle(String.format("%s %s %s %s", transactionType, payingID, receivingID, "")));
        assertFalse(validator.handle(String.format("%s %s %s %s", transactionType, payingID, receivingID, "^*(FGd")));
        assertTrue(validator.handle(String.format("%s %s %s %s", transactionType, payingID, receivingID, transferAmount)));
    }

    @Test
    protected void transfer_amounts_from_checking_to_checking_should_be_greater_than_0() {
        String payingID = CHECKING_ID_0;
        String receivingID = CHECKING_ID_1;
        double transferAmount = 0;

        assertFalse(validator.handle(String.format("%s %s %s %s", transactionType, payingID, receivingID, transferAmount - 300)));
        assertFalse(validator.handle(String.format("%s %s %s %s", transactionType, payingID, receivingID, transferAmount - 3)));
        assertFalse(validator.handle(String.format("%s %s %s %s", transactionType, payingID, receivingID, transferAmount)));
        assertTrue(validator.handle(String.format("%s %s %s %s", transactionType, payingID, receivingID, transferAmount + 3)));
    }

    @Test
    protected void transfer_amounts_from_checking_to_checking_should_be_less_than_or_equal_to_400() {
        String payingID = CHECKING_ID_1;
        String receivingID = CHECKING_ID_0;
        double transferAmount = 400;

        assertTrue(validator.handle(String.format("%s %s %s %s", transactionType, payingID, receivingID, transferAmount - 3)));
        assertTrue(validator.handle(String.format("%s %s %s %s", transactionType, payingID, receivingID, transferAmount)));
        assertFalse(validator.handle(String.format("%s %s %s %s", transactionType, payingID, receivingID, transferAmount + 3)));
        assertFalse(validator.handle(String.format("%s %s %s %s", transactionType, payingID, receivingID, transferAmount + 300)));
    }

    @Test
    protected void transfer_amounts_from_checking_to_savings_should_be_greater_than_0() {
        String payingID = CHECKING_ID_0;
        String receivingID = SAVINGS_ID_0;
        double transferAmount = 0;

        assertFalse(validator.handle(String.format("%s %s %s %s", transactionType, payingID, receivingID, transferAmount - 300)));
        assertFalse(validator.handle(String.format("%s %s %s %s", transactionType, payingID, receivingID, transferAmount - 3)));
        assertFalse(validator.handle(String.format("%s %s %s %s", transactionType, payingID, receivingID, transferAmount)));
        assertTrue(validator.handle(String.format("%s %s %s %s", transactionType, payingID, receivingID, transferAmount + 3)));
    }

    @Test
    protected void transfer_amounts_from_checking_to_savings_should_be_less_than_or_equal_to_400() {
        String payingID = CHECKING_ID_1;
        String receivingID = SAVINGS_ID_1;
        double transferAmount = 400;

        assertTrue(validator.handle(String.format("%s %s %s %s", transactionType, payingID, receivingID, transferAmount - 3)));
        assertTrue(validator.handle(String.format("%s %s %s %s", transactionType, payingID, receivingID, transferAmount)));
        assertFalse(validator.handle(String.format("%s %s %s %s", transactionType, payingID, receivingID, transferAmount + 3)));
        assertFalse(validator.handle(String.format("%s %s %s %s", transactionType, payingID, receivingID, transferAmount + 300)));
    }

    @Test
    protected void transfer_amounts_from_savings_to_checking_should_be_greater_than_0() {
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
    protected void transfer_amounts_from_savings_to_checking_should_be_less_than_or_equal_to_1000() {
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
    protected void transfer_amounts_from_savings_to_savings_should_be_greater_than_0() {
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
    protected void transfer_amounts_from_savings_to_savings_should_be_less_than_or_equal_to_1000() {
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
    protected void transfer_amounts_to_cd_are_invalid() {
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
    protected void transfer_amounts_from_cd_to_savings_should_be_between_the_paying_account_balance_and_2500_inclusive() {
        cdBalance = 2200;

        List<Integer> months = Arrays.asList(getMonthsPerYear(), bank.getMaxTimeTravel());
        String payingID = CD_ID_1;
        String receivingID = SAVINGS_ID_0;
        List<Double> lowerBound = new ArrayList<>();
        double upperBound = 2500;

        bank.removeAccount(payingID);
        bank.createCDAccount(payingID, cdBalance);
        bank.deposit(receivingID, bank.getAccount(receivingID).getMaxDepositAmount());
        lowerBound.add(timeTravel(cdBalance, bank, months.get(0)));
        lowerBound.add(timeTravel(lowerBound.get(0), bank, months.get(1)));

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
    protected void transfer_validators_can_ignore_additional_arguments() {
        String payingID = CD_ID_1;
        String receivingID = SAVINGS_ID_1;
        double transferAmount = min(bank.getAccount(payingID).getMaxWithdrawAmount(), bank.getAccount(receivingID).getMaxDepositAmount());

        bank.timeTravel(12);

        assertTrue(validator.handle(String.format("%s %s %s %s %s", transactionType, payingID, receivingID, transferAmount, "nuke")));
        assertTrue(validator.handle(String.format("%s %s %s %s  %s  %s %s  %s   %s", transactionType, payingID, receivingID, transferAmount, "DsDifJ", "paSJiOf", "ps3f&jf", "sp@&HR*&HDSoa", "psd)(Jo")));
    }

    @Test
    protected void transfer_validators_are_case_insensitive() {
        String payingID = CHECKING_ID_1;
        String receivingID = CHECKING_ID_0;
        double transferAmount = min(bank.getAccount(payingID).getMaxWithdrawAmount(), bank.getAccount(receivingID).getMaxDepositAmount());

        assertTrue(validator.handle(String.format("traNSFer %s %s %s", payingID, receivingID, transferAmount)));
    }

    @Test
    protected void transfer_validators_can_be_in_a_chain_of_responsibility() {
        bank = new Bank();
        validator = new TransferValidator(bank);

        AccountType accountType = AccountType.SAVINGS;

        validator.setNext(new TimeTravelValidator(bank));

        assertTrue(validator.handle(String.format("%s %s", TransactionType.TimeTravel, MONTHS)));
        assertFalse(validator.handle(String.format("%s %s %s", TransactionType.Create, accountType, SAVINGS_ID_1)));
    }
}
