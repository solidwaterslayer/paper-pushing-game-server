package server.game.pushing.paper.validator.transaction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.game.pushing.paper.bank.Bank;
import server.game.pushing.paper.bank.account.CD;
import server.game.pushing.paper.bank.account.Checking;
import server.game.pushing.paper.bank.account.Savings;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DepositValidatorTests {
    protected DepositValidator depositValidator;
    protected Bank bank;

    protected final String CHECKING_ID = "00000000";
    protected final String SAVINGS_ID = "00000001";
    protected final String CD_ID = "00000010";
    protected final double APR = 0.1;
    protected final double INITIAL_CD_BALANCE = 1000;

    @BeforeEach
    protected void setUp() {
        bank = new Bank(Arrays.asList(
                new Checking(CHECKING_ID, APR),
                new Savings(SAVINGS_ID, APR),
                new CD(CD_ID, APR, INITIAL_CD_BALANCE)
        ));

        depositValidator = new DepositValidator(bank);
    }

    @Test
    protected void deposit_validator_when_transaction_is_not_valid_should_pass_transaction_up_the_chain_of_responsibility() {
        depositValidator.setNextHandler(new WithdrawValidator(bank));
        bank.createSavings("00000000", 0);
        bank.createChecking("00000001", 0);

        assertTrue(depositValidator.handle("withdraw 00000000 1000"));
        assertFalse(depositValidator.handle("transfer 00000000 00000001 1000"));
    }

    @Test
    protected void transaction_should_contain_the_transaction_type_deposit_as_the_first_argument() {
        assertFalse(depositValidator.handle(""));
        assertFalse(depositValidator.handle(String.format(" %s 1000", CHECKING_ID)));
        assertFalse(depositValidator.handle(String.format("nuke %s 1000", CHECKING_ID)));
        assertTrue(depositValidator.handle(String.format("deposit %s 1000", CHECKING_ID)));
    }

    @Test
    protected void transaction_should_contain_a_taken_id_as_the_second_argument() {
        assertFalse(depositValidator.handle("deposit 47386825 1000"));

        assertFalse(depositValidator.handle("deposit"));
        assertFalse(depositValidator.handle("deposit  1000"));

        assertTrue(depositValidator.handle(String.format("deposit %s 1000", CHECKING_ID)));
        assertTrue(depositValidator.handle(String.format("deposit %s 2500", SAVINGS_ID)));
    }

    @Test
    protected void transaction_should_contain_a_deposit_amount_as_the_third_argument() {
        assertFalse(depositValidator.handle(String.format("deposit %s 7g8Y&*", SAVINGS_ID)));
        assertFalse(depositValidator.handle(String.format("deposit %s", SAVINGS_ID)));
        assertTrue(depositValidator.handle(String.format("deposit %s 2500", SAVINGS_ID)));
    }

    @Test
    protected void transaction_when_account_type_is_checking_should_contain_a_deposit_amount_greater_than_0() {
        assertFalse(depositValidator.handle(String.format("deposit %s -500", CHECKING_ID)));

        assertFalse(depositValidator.handle(String.format("deposit %s -100", CHECKING_ID)));
        assertFalse(depositValidator.handle(String.format("deposit %s 0", CHECKING_ID)));
        assertTrue(depositValidator.handle(String.format("deposit %s 100", CHECKING_ID)));

        assertTrue(depositValidator.handle(String.format("deposit %s 500", CHECKING_ID)));
    }

    @Test
    protected void transaction_when_account_type_is_checking_should_contain_a_deposit_amount_less_than_or_equal_to_1000() {
        assertTrue(depositValidator.handle(String.format("deposit %s 600", CHECKING_ID)));

        assertTrue(depositValidator.handle(String.format("deposit %s 900", CHECKING_ID)));
        assertTrue(depositValidator.handle(String.format("deposit %s 1000", CHECKING_ID)));
        assertFalse(depositValidator.handle(String.format("deposit %s 1100", CHECKING_ID)));

        assertFalse(depositValidator.handle(String.format("deposit %s 2000", CHECKING_ID)));
    }

    @Test
    protected void transaction_when_account_type_is_savings_should_contain_a_deposit_amount_greater_than_0() {
        assertFalse(depositValidator.handle(String.format("deposit %s -500", SAVINGS_ID)));

        assertFalse(depositValidator.handle(String.format("deposit %s -100", SAVINGS_ID)));
        assertFalse(depositValidator.handle(String.format("deposit %s 0", SAVINGS_ID)));
        assertTrue(depositValidator.handle(String.format("deposit %s 100", SAVINGS_ID)));

        assertTrue(depositValidator.handle(String.format("deposit %s 1200", SAVINGS_ID)));
    }

    @Test
    protected void transaction_when_account_type_is_savings_should_contain_a_deposit_amount_less_than_or_equal_to_2500() {
        assertTrue(depositValidator.handle(String.format("deposit %s 1300", SAVINGS_ID)));

        assertTrue(depositValidator.handle(String.format("deposit %s 2400", SAVINGS_ID)));
        assertTrue(depositValidator.handle(String.format("deposit %s 2500", SAVINGS_ID)));
        assertFalse(depositValidator.handle(String.format("deposit %s 2600", SAVINGS_ID)));

        assertFalse(depositValidator.handle(String.format("deposit %s 5000", SAVINGS_ID)));
    }

    @Test
    protected void transaction_when_account_type_is_cd_should_not_be_possible() {
        assertFalse(depositValidator.handle(String.format("deposit %s -1000", CD_ID)));

        assertFalse(depositValidator.handle(String.format("deposit %s -100", CD_ID)));
        assertFalse(depositValidator.handle(String.format("deposit %s 0", CD_ID)));
        assertFalse(depositValidator.handle(String.format("deposit %s 100", CD_ID)));

        assertFalse(depositValidator.handle(String.format("deposit %s 1200", CD_ID)));
        assertFalse(depositValidator.handle(String.format("deposit %s 1300", CD_ID)));

        assertFalse(depositValidator.handle(String.format("deposit %s 2400", CD_ID)));
        assertFalse(depositValidator.handle(String.format("deposit %s 2500", CD_ID)));
        assertFalse(depositValidator.handle(String.format("deposit %s 2600", CD_ID)));

        assertFalse(depositValidator.handle(String.format("deposit %s 5000", CD_ID)));
    }

    @Test
    protected void transaction_should_be_case_insensitive() {
        assertTrue(depositValidator.handle("DEPOSIT 00000000 1000"));
        assertTrue(depositValidator.handle("dePOSit 00000001 2500"));
    }

    @Test
    protected void transaction_should_be_possible_with_useless_additional_arguments() {
        assertTrue(depositValidator.handle("deposit 00000000 1000 0 0 0 0 0 0 0"));
        assertTrue(depositValidator.handle("deposit 00000001 2500 nuke"));
    }
}
