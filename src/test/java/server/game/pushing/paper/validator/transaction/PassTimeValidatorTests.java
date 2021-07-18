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
        passTimeValidator = new PassTimeValidator(bank);
    }

    @Test
    protected void pass_time_validator_when_transaction_is_not_valid_should_pass_transaction_up_the_chain_of_responsibility() {
        passTimeValidator.setNext(new CreateValidator(bank));
        bank.createChecking("00000000", 0.5);

        assertFalse(passTimeValidator.handle("create cd 0 10000"));
        assertFalse(passTimeValidator.handle("deposit 00000000 1000"));
    }

    @Test
    protected void transaction_should_contain_the_transaction_type_pass_time_as_the_first_and_second_argument() {
        int months = 10;


        assertFalse(passTimeValidator.handle(""));
        assertFalse(passTimeValidator.handle(String.format("  %d", months)));

        assertFalse(passTimeValidator.handle("pass"));
        assertFalse(passTimeValidator.handle(String.format("pass  %d", months)));
        assertFalse(passTimeValidator.handle(String.format("pass nuke %d", months)));

        assertFalse(passTimeValidator.handle(String.format(" time %d", months)));
        assertFalse(passTimeValidator.handle(String.format("nuke time %d", months)));

        assertTrue(passTimeValidator.handle(String.format("pass time %d", months)));
    }

    @Test
    protected void transaction_should_contain_months_as_the_third_argument() {
        assertFalse(passTimeValidator.handle("pass time"));
        assertFalse(passTimeValidator.handle("pass time T^*23"));
        assertTrue(passTimeValidator.handle("pass time 50"));
    }

    @Test
    protected void transaction_should_contain_months_between_1_and_60_inclusive() {
        assertFalse(passTimeValidator.handle("pass time -10"));

        assertFalse(passTimeValidator.handle("pass time 0"));
        assertTrue(passTimeValidator.handle("pass time 1"));
        assertTrue(passTimeValidator.handle("pass time 2"));

        assertTrue(passTimeValidator.handle("pass time 30"));
        assertTrue(passTimeValidator.handle("pass time 40"));

        assertTrue(passTimeValidator.handle("pass time 50"));
        assertTrue(passTimeValidator.handle("pass time 60"));
        assertFalse(passTimeValidator.handle("pass time 70"));

        assertFalse(passTimeValidator.handle("pass time 100"));
    }

    @Test
    protected void transaction_should_be_case_insensitive() {
        int months = 20;

        assertTrue(passTimeValidator.handle(String.format("pAss time %d", months)));
        assertTrue(passTimeValidator.handle(String.format("pass tiMe %d", months)));
        assertTrue(passTimeValidator.handle(String.format("pAss tiMe %d", months)));
    }

    @Test
    protected void transaction_should_be_possible_with_useless_additional_arguments() {
        int months = 30;

        assertTrue(passTimeValidator.handle(String.format("pass time %d nuke", months)));
        assertTrue(passTimeValidator.handle(String.format("pass time %d 38 38 uFjH$d fdu 8 3 o2 j djf fj8 2", months)));
    }
}
