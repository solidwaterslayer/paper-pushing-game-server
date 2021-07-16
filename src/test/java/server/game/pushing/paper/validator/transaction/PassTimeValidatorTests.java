package server.game.pushing.paper.validator.transaction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.game.pushing.paper.bank.Bank;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PassTimeValidatorTests {
    protected PassTimeValidator passTimeValidator;
    protected Bank bank;

    @BeforeEach
    protected void setUp() {
        bank = new Bank();
        passTimeValidator = new PassTimeValidator(null, bank);
    }

    @Test
    protected void pass_time_validator_when_transaction_is_not_valid_should_pass_transaction_up_the_chain_of_responsibility() {
        passTimeValidator = new PassTimeValidator(new CreateValidator(null, bank), bank);

        bank.createChecking("00000000", 0.5);

        assertFalse(passTimeValidator.isTransactionValid("create cd 0 10000"));
        assertFalse(passTimeValidator.isTransactionValid("deposit 00000000 1000"));
    }

    @Test
    protected void transaction_should_contain_the_transaction_type_pass_time_as_the_first_and_second_argument() {
        assertFalse(passTimeValidator.isTransactionValid(""));
        assertFalse(passTimeValidator.isTransactionValid("  10"));

        assertFalse(passTimeValidator.isTransactionValid("pass"));
        assertFalse(passTimeValidator.isTransactionValid("pass  10"));
        assertFalse(passTimeValidator.isTransactionValid("pass nuke 10"));
        assertFalse(passTimeValidator.isTransactionValid("  time 10"));
        assertFalse(passTimeValidator.isTransactionValid("nuke time 10"));

        assertTrue(passTimeValidator.isTransactionValid("pass time 10"));
    }

    @Test
    protected void transaction_should_contain_a_pass_time_as_the_third_argument() {
        assertFalse(passTimeValidator.isTransactionValid("pass time"));
        assertFalse(passTimeValidator.isTransactionValid("pass time T^*23"));
        assertTrue(passTimeValidator.isTransactionValid("pass time 50"));
    }

    @Test
    protected void transaction_should_contain_a_pass_time_between_1_and_60_inclusive() {
        assertFalse(passTimeValidator.isTransactionValid("pass time -10"));

        assertFalse(passTimeValidator.isTransactionValid("pass time 0"));
        assertTrue(passTimeValidator.isTransactionValid("pass time 1"));
        assertTrue(passTimeValidator.isTransactionValid("pass time 2"));

        assertTrue(passTimeValidator.isTransactionValid("pass time 30"));
        assertTrue(passTimeValidator.isTransactionValid("pass time 40"));

        assertTrue(passTimeValidator.isTransactionValid("pass time 50"));
        assertTrue(passTimeValidator.isTransactionValid("pass time 60"));
        assertFalse(passTimeValidator.isTransactionValid("pass time 70"));

        assertFalse(passTimeValidator.isTransactionValid("pass time 100"));
    }

    @Test
    protected void transaction_should_be_case_insensitive() {
        assertTrue(passTimeValidator.isTransactionValid("pAss time 20"));
        assertTrue(passTimeValidator.isTransactionValid("pass tiMe 20"));
        assertTrue(passTimeValidator.isTransactionValid("pAss tiMe 20"));
    }

    @Test
    protected void transaction_should_be_possible_with_useless_additional_arguments() {
        assertTrue(passTimeValidator.isTransactionValid("pass time 30 nuke"));
        assertTrue(passTimeValidator.isTransactionValid("pass time 30 38 38 uFjh%d fdu 8 3 o2 j djf fj8 2"));
    }
}
