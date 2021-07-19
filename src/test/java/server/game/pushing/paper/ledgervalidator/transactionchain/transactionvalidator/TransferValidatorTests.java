package server.game.pushing.paper.ledgervalidator.transactionchain.transactionvalidator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.game.pushing.paper.ledgervalidator.bank.Bank;
import server.game.pushing.paper.ledgervalidator.bank.BankTests;
import server.game.pushing.paper.ledgervalidator.bank.account.AccountType;
import server.game.pushing.paper.ledgervalidator.bank.account.CD;
import server.game.pushing.paper.ledgervalidator.bank.account.Checking;
import server.game.pushing.paper.ledgervalidator.bank.account.Savings;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class TransferValidatorTests {
    protected TransferValidator transferValidator;
    protected Bank bank;

    protected final String CHECKING_ID_0 = "00000000";
    protected final String CHECKING_ID_1 = "10000000";
    protected final String SAVINGS_ID_0 = "00000001";
    protected final String SAVINGS_ID_1 = "10000001";
    protected final String CD_ID = "00000010";
    protected final double APR = 0.3;
    protected final double INITIAL_CD_BALANCE = 1000;

    protected final double CD_TRANSFER_AMOUNT = INITIAL_CD_BALANCE * 2;

    @BeforeEach
    protected void setUp() {
        bank = new Bank(Arrays.asList(
                new Checking(CHECKING_ID_0, APR),
                new Checking(CHECKING_ID_1, APR),
                new Savings(SAVINGS_ID_0, APR),
                new Savings(SAVINGS_ID_1, APR),
                new CD(CD_ID, APR, INITIAL_CD_BALANCE)
        ));
        bank.deposit(CHECKING_ID_0, 1000);
        bank.deposit(CHECKING_ID_1, 1000);
        bank.deposit(SAVINGS_ID_0, 2500);
        bank.deposit(SAVINGS_ID_1, 2500);

        transferValidator = new TransferValidator(bank);
    }

    @Test
    protected void transfer_validator_when_transaction_is_not_valid_should_pass_transaction_up_the_chain_of_responsibility() {
        transferValidator.setNext(new PassTimeValidator(bank));
        bank.createSavings("00000000", 0);
        bank.createChecking("00000001", 0);

        assertTrue(transferValidator.handle("pass time 60"));
        assertFalse(transferValidator.handle("create cd 0 10000"));
    }

    @Test
    protected void transaction_should_contain_the_transaction_type_transfer_as_the_first_argument() {
        assertFalse(transferValidator.handle(""));
        assertFalse(transferValidator.handle(String.format(" %s %s 400", CHECKING_ID_1, CHECKING_ID_0)));
        assertFalse(transferValidator.handle(String.format("nuke %s %s 400", CHECKING_ID_1, CHECKING_ID_0)));
        assertTrue(transferValidator.handle(String.format("transfer %s %s 400", CHECKING_ID_1, CHECKING_ID_0)));
    }

    @Test
    protected void transaction_should_contain_a_unique_and_taken_from_and_to_id_as_the_second_and_third_argument() {
        double transferAmount = 1000;

        assertFalse(transferValidator.handle(String.format("transfer %s %s %f", SAVINGS_ID_1, SAVINGS_ID_1, transferAmount)));

        assertFalse(transferValidator.handle(String.format("transfer %s %s %f", "34782794", SAVINGS_ID_0, transferAmount)));
        assertFalse(transferValidator.handle(String.format("transfer %s %s %f", SAVINGS_ID_1, "78344279", transferAmount)));

        assertFalse(transferValidator.handle(String.format("transfer  %s %f", SAVINGS_ID_0, transferAmount)));
        assertFalse(transferValidator.handle(String.format("transfer %s  %f", SAVINGS_ID_1, transferAmount)));

        assertTrue(transferValidator.handle(String.format("transfer %s %s %f", SAVINGS_ID_1, SAVINGS_ID_0, transferAmount)));
    }

    @Test
    protected void transaction_should_contain_a_transfer_amount_as_the_fourth_argument() {
        bank.passTime(12);

        assertFalse(transferValidator.handle(String.format("transfer %s %s 7g8Y&*", CD_ID, SAVINGS_ID_1)));
        assertFalse(transferValidator.handle(String.format("transfer %s %s", CD_ID, SAVINGS_ID_0)));
        assertTrue(transferValidator.handle(String.format("transfer %s %s %f", CD_ID, SAVINGS_ID_1, CD_TRANSFER_AMOUNT)));
    }

    @Test
    protected void transaction_when_account_type_is_from_checking_to_checking_should_contain_a_transfer_amount_greater_than_0() {
        assertFalse(transferValidator.handle(String.format("transfer %s %s -300", CHECKING_ID_0, CHECKING_ID_1)));

        assertFalse(transferValidator.handle(String.format("transfer %s %s -100", CHECKING_ID_0, CHECKING_ID_1)));
        assertFalse(transferValidator.handle(String.format("transfer %s %s 0", CHECKING_ID_0, CHECKING_ID_1)));
        assertTrue(transferValidator.handle(String.format("transfer %s %s 100", CHECKING_ID_0, CHECKING_ID_1)));

        assertTrue(transferValidator.handle(String.format("transfer %s %s 250", CHECKING_ID_0, CHECKING_ID_1)));
    }

    @Test
    protected void transaction_when_account_type_is_from_checking_to_checking_should_contain_a_transfer_amount_less_than_or_equal_to_400() {
        assertTrue(transferValidator.handle(String.format("transfer %s %s 150", CHECKING_ID_1, CHECKING_ID_0)));

        assertTrue(transferValidator.handle(String.format("transfer %s %s 300", CHECKING_ID_1, CHECKING_ID_0)));
        assertTrue(transferValidator.handle(String.format("transfer %s %s 400", CHECKING_ID_1, CHECKING_ID_0)));
        assertFalse(transferValidator.handle(String.format("transfer %s %s 500", CHECKING_ID_1, CHECKING_ID_0)));

        assertFalse(transferValidator.handle(String.format("transfer %s %s 1000", CHECKING_ID_1, CHECKING_ID_0)));
    }

    @Test
    protected void transaction_when_account_type_is_from_checking_to_savings_should_contain_a_transfer_amount_greater_than_0() {
        assertFalse(transferValidator.handle(String.format("transfer %s %s -300", CHECKING_ID_0, SAVINGS_ID_0)));

        assertFalse(transferValidator.handle(String.format("transfer %s %s -100", CHECKING_ID_0, SAVINGS_ID_0)));
        assertFalse(transferValidator.handle(String.format("transfer %s %s 0", CHECKING_ID_0, SAVINGS_ID_0)));
        assertTrue(transferValidator.handle(String.format("transfer %s %s 100", CHECKING_ID_0, SAVINGS_ID_0)));

        assertTrue(transferValidator.handle(String.format("transfer %s %s 250", CHECKING_ID_0, SAVINGS_ID_0)));
    }

    @Test
    protected void transaction_when_account_type_is_from_checking_to_savings_should_contain_a_transfer_amount_less_than_or_equal_to_400() {
        assertTrue(transferValidator.handle(String.format("transfer %s %s 150", CHECKING_ID_0, SAVINGS_ID_1)));

        assertTrue(transferValidator.handle(String.format("transfer %s %s 300", CHECKING_ID_0, SAVINGS_ID_1)));
        assertTrue(transferValidator.handle(String.format("transfer %s %s 400", CHECKING_ID_0, SAVINGS_ID_1)));
        assertFalse(transferValidator.handle(String.format("transfer %s %s 500", CHECKING_ID_0, SAVINGS_ID_1)));

        assertFalse(transferValidator.handle(String.format("transfer %s %s 1000", CHECKING_ID_0, SAVINGS_ID_1)));
    }

    @Test
    protected void transfer_from_savings_twice_a_month_or_more_should_not_be_possible() {
        double transferAmount = 400;

        assertTrue(transferValidator.handle(String.format("transfer %s %s %f", SAVINGS_ID_1, CHECKING_ID_1, transferAmount)));

        bank.passTime(1);
        bank.transfer(SAVINGS_ID_1, CHECKING_ID_1, transferAmount);
        assertFalse(transferValidator.handle(String.format("transfer %s %s %f", SAVINGS_ID_1, CHECKING_ID_1, transferAmount)));

        bank.passTime(1);
        bank.transfer(SAVINGS_ID_1, CHECKING_ID_1, transferAmount);
        bank.passTime(1);
        assertTrue(transferValidator.handle(String.format("transfer %s %s %f", SAVINGS_ID_1, CHECKING_ID_1, transferAmount)));
    }

    @Test
    protected void transaction_when_account_type_is_from_savings_to_checking_should_contain_a_transfer_amount_greater_than_0() {
        assertFalse(transferValidator.handle(String.format("transfer %s %s -300", SAVINGS_ID_0, CHECKING_ID_1)));

        assertFalse(transferValidator.handle(String.format("transfer %s %s -100", SAVINGS_ID_0, CHECKING_ID_1)));
        assertFalse(transferValidator.handle(String.format("transfer %s %s 0", SAVINGS_ID_0, CHECKING_ID_1)));
        assertTrue(transferValidator.handle(String.format("transfer %s %s 100", SAVINGS_ID_0, CHECKING_ID_1)));

        assertTrue(transferValidator.handle(String.format("transfer %s %s 500", SAVINGS_ID_0, CHECKING_ID_1)));
    }

    @Test
    protected void transaction_when_account_type_is_from_savings_to_checking_should_contain_a_transfer_amount_less_than_or_equal_to_1000() {
        assertTrue(transferValidator.handle(String.format("transfer %s %s 600", SAVINGS_ID_1, CHECKING_ID_1)));

        assertTrue(transferValidator.handle(String.format("transfer %s %s 900", SAVINGS_ID_1, CHECKING_ID_1)));
        assertTrue(transferValidator.handle(String.format("transfer %s %s 1000", SAVINGS_ID_1, CHECKING_ID_1)));
        assertFalse(transferValidator.handle(String.format("transfer %s %s 1100", SAVINGS_ID_1, CHECKING_ID_1)));

        assertFalse(transferValidator.handle(String.format("transfer %s %s 2000", SAVINGS_ID_1, CHECKING_ID_1)));
    }

    @Test
    protected void transaction_when_account_type_is_from_savings_to_savings_should_contain_a_transfer_amount_greater_than_0() {
        assertFalse(transferValidator.handle(String.format("transfer %s %s -300", SAVINGS_ID_0, SAVINGS_ID_1)));

        assertFalse(transferValidator.handle(String.format("transfer %s %s -100", SAVINGS_ID_0, SAVINGS_ID_1)));
        assertFalse(transferValidator.handle(String.format("transfer %s %s 0", SAVINGS_ID_0, SAVINGS_ID_1)));
        assertTrue(transferValidator.handle(String.format("transfer %s %s 100", SAVINGS_ID_0, SAVINGS_ID_1)));

        assertTrue(transferValidator.handle(String.format("transfer %s %s 500", SAVINGS_ID_0, SAVINGS_ID_1)));
    }

    @Test
    protected void transaction_when_account_type_is_from_savings_to_savings_should_contain_a_transfer_amount_less_than_or_equal_to_1000() {
        assertTrue(transferValidator.handle(String.format("transfer %s %s 600", SAVINGS_ID_1, SAVINGS_ID_0)));

        assertTrue(transferValidator.handle(String.format("transfer %s %s 900", SAVINGS_ID_1, SAVINGS_ID_0)));
        assertTrue(transferValidator.handle(String.format("transfer %s %s 1000", SAVINGS_ID_1, SAVINGS_ID_0)));
        assertFalse(transferValidator.handle(String.format("transfer %s %s 1100", SAVINGS_ID_1, SAVINGS_ID_0)));

        assertFalse(transferValidator.handle(String.format("transfer %s %s 2000", SAVINGS_ID_1, SAVINGS_ID_0)));
    }

    @Test
    protected void transfer_to_cd_should_not_be_possible() {
        assertFalse(transferValidator.handle(String.format("transfer %s %s -1000", CHECKING_ID_0, CD_ID)));

        assertFalse(transferValidator.handle(String.format("transfer %s %s -100", CHECKING_ID_1, CD_ID)));
        assertFalse(transferValidator.handle(String.format("transfer %s %s 0", SAVINGS_ID_0, CD_ID)));
        assertFalse(transferValidator.handle(String.format("transfer %s %s 100", SAVINGS_ID_1, CD_ID)));

        assertFalse(transferValidator.handle(String.format("transfer %s %s 1200", CHECKING_ID_0, CD_ID)));
        assertFalse(transferValidator.handle(String.format("transfer %s %s 1300", CHECKING_ID_1, CD_ID)));

        assertFalse(transferValidator.handle(String.format("transfer %s %s 2400", SAVINGS_ID_0, CD_ID)));
        assertFalse(transferValidator.handle(String.format("transfer %s %s 2500", SAVINGS_ID_1, CD_ID)));
        assertFalse(transferValidator.handle(String.format("transfer %s %s 2600", CHECKING_ID_0, CD_ID)));

        assertFalse(transferValidator.handle(String.format("transfer %s %s 5000", SAVINGS_ID_1, CD_ID)));
    }

    @Test
    protected void transfer_from_cd_to_savings_should_be_possible_after_12_month_inclusive() {
        for (int i = 0; i < 24; i++) {
            assertEquals(i >= 12, transferValidator.handle(String.format("transfer %s %s %f", CD_ID, SAVINGS_ID_0, CD_TRANSFER_AMOUNT)));

            bank.passTime(1);
        }
    }

    @Test
    protected void transfer_from_cd_to_savings_should_be_greater_than_or_equal_to_balance() {
        int months = 12;
        double cdWithdrawAmount = BankTests.passTime(APR, bank.getMinBalanceFee(), AccountType.CD, INITIAL_CD_BALANCE, months);

        bank.deposit(SAVINGS_ID_1, 2500);
        bank.passTime(months);

        assertFalse(transferValidator.handle(String.format("transfer %s %s %f", CD_ID, SAVINGS_ID_1, cdWithdrawAmount - 500)));

        assertFalse(transferValidator.handle(String.format("transfer %s %s %f", CD_ID, SAVINGS_ID_1, cdWithdrawAmount - 100)));
        assertEquals(cdWithdrawAmount, bank.getAccount(CD_ID).getBalance());
        assertTrue(transferValidator.handle(String.format("transfer %s %s %.20f", CD_ID, SAVINGS_ID_1, cdWithdrawAmount)));
        assertTrue(transferValidator.handle(String.format("transfer %s %s %f", CD_ID, SAVINGS_ID_1, cdWithdrawAmount + 100)));

        assertTrue(transferValidator.handle(String.format("transfer %s %s %f", CD_ID, SAVINGS_ID_1, cdWithdrawAmount + 500)));
    }

    @Test
    protected void transaction_should_be_case_insensitive() {
        assertTrue(transferValidator.handle(String.format("traNSFer %s %s 400", CHECKING_ID_1, CHECKING_ID_0)));
    }

    @Test
    protected void transaction_should_be_possible_with_useless_additional_arguments() {
        bank.passTime(12);
        assertTrue(transferValidator.handle(String.format("transfer %s %s %f nuke", CD_ID, SAVINGS_ID_1, CD_TRANSFER_AMOUNT)));
        assertTrue(transferValidator.handle(String.format("transfer %s %s %f 0    0 0     0  0 0  0 0", CD_ID, SAVINGS_ID_1, CD_TRANSFER_AMOUNT)));
    }
}
