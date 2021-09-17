package server.game.pushing.paper.ledgervalidator.transactionchain.transactionvalidator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.game.pushing.paper.ledgervalidator.bank.Bank;
import server.game.pushing.paper.ledgervalidator.bank.account.AccountType;
import server.game.pushing.paper.ledgervalidator.bank.account.CD;
import server.game.pushing.paper.ledgervalidator.bank.account.Checking;
import server.game.pushing.paper.ledgervalidator.bank.account.Savings;
import server.game.pushing.paper.ledgervalidator.transactionchain.TransactionType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.Math.min;
import static org.junit.jupiter.api.Assertions.*;
import static server.game.pushing.paper.ledgervalidator.bank.Bank.*;
import static server.game.pushing.paper.ledgervalidator.bank.BankTests.passTime;

public class TransferValidatorTests {
    protected Bank bank;
    protected TransferValidator transferValidator;

    protected final String CHECKING_ID_0 = "87439753";
    protected final String CHECKING_ID_1 = "87439742";
    protected final String SAVINGS_ID_0 = "98430842";
    protected final String SAVINGS_ID_1 = "98430854";
    protected final String CD_ID_0 = "24799348";
    protected final String CD_ID_1 = "14799348";
    protected final double APR = getMaxAPR();
    protected final double INITIAL_CD_BALANCE = getMinInitialCDBalance();

    @BeforeEach
    protected void setUp() {
        bank = new Bank(Arrays.asList(
                new Checking(CHECKING_ID_0, APR),
                new Checking(CHECKING_ID_1, APR),
                new Savings(SAVINGS_ID_0, APR),
                new Savings(SAVINGS_ID_1, APR),
                new CD(CD_ID_0, APR, INITIAL_CD_BALANCE),
                new CD(CD_ID_1, APR, INITIAL_CD_BALANCE)
        ));
        transferValidator = new TransferValidator(bank);

        bank.deposit(CHECKING_ID_0, Checking.getMaxWithdrawAmount());
        bank.deposit(CHECKING_ID_1, Checking.getMaxDepositAmount());
        bank.deposit(SAVINGS_ID_0, Savings.getMaxDepositAmount());
        bank.deposit(SAVINGS_ID_1, Savings.getMaxDepositAmount());
    }

    @Test
    protected void transfer_validator_when_transaction_is_not_valid_should_pass_transaction_up_the_chain_of_responsibility() {
        int months = getMonthsPerYear();
        AccountType accountType = AccountType.Savings;
        String id = "97420734";
        double apr = getMaxAPR();

        transferValidator.setNext(new PassTimeValidator(bank));

        assertTrue(transferValidator.handle(String.format("%s %s", TransactionType.PassTime, months)));
        assertFalse(transferValidator.handle(String.format("%s %s %s %s", TransactionType.Create, accountType, id, apr)));
    }

    @Test
    protected void transaction_should_contain_the_transaction_type_transfer_as_the_first_argument() {
        TransactionType transactionType = TransactionType.Transfer;
        String fromID = CHECKING_ID_1;
        String toID = CHECKING_ID_0;
        double transferAmount = min(Checking.getMaxWithdrawAmount(), Checking.getMaxDepositAmount());

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
        double transferAmount = min(Savings.getMaxWithdrawAmount(), Savings.getMaxDepositAmount());

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
        double transferAmount = min(CD.getMaxWithdrawAmount(), Savings.getMaxDepositAmount());

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
        double transferAmount = min(Savings.getMaxWithdrawAmount(), Checking.getMaxDepositAmount());
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
            assertEquals(month >= monthsPerYear, transferValidator.handle(String.format("%s %s %s %s", TransactionType.Transfer, CD_ID_0, SAVINGS_ID_0, Savings.getMaxDepositAmount())));

            bank.passTime(1);
        }
    }

    @Test
    protected void transfer_from_cd_to_savings_should_be_between_balance_and_2500_inclusive() {
        double cdAPR = 0.6;
        double initialCDBalance = 2200;

        List<Integer> months = Arrays.asList(getMonthsPerYear(), 60);
        TransactionType transactionType = TransactionType.Transfer;
        String fromID = CD_ID_1;
        String toID = SAVINGS_ID_0;
        List<Double> lowerBound = new ArrayList<>();
        double upperBound = 2500;

        bank.removeAccount(fromID);
        bank.createCD(fromID, cdAPR, initialCDBalance);
        bank.deposit(toID, Savings.getMaxDepositAmount());
        lowerBound.add(passTime(cdAPR, bank.getMinBalanceFee(), AccountType.CD, initialCDBalance, months.get(0)));
        lowerBound.add(passTime(cdAPR, bank.getMinBalanceFee(), AccountType.CD, lowerBound.get(0), months.get(1)));

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
        assertTrue(transferValidator.handle(String.format("traNSFer %s %s %s", CHECKING_ID_1, CHECKING_ID_0, 400)));
    }

    @Test
    protected void transaction_should_be_possible_with_useless_additional_arguments() {
        TransactionType transactionType = TransactionType.Transfer;
        String fromID = CD_ID_1;
        String toID = SAVINGS_ID_1;
        double transferAmount = min(CD.getMaxWithdrawAmount(), Savings.getMaxDepositAmount());

        bank.passTime(12);

        assertTrue(transferValidator.handle(String.format("%s %s %s %s nuke", transactionType, fromID, toID, transferAmount)));
        assertTrue(transferValidator.handle(String.format("%s %s %s %s  DsDifJ paSJiOf ps3f&jf sp@&HR*&HDSoa psd)(Jo", transactionType, fromID, toID, transferAmount)));
    }
}
