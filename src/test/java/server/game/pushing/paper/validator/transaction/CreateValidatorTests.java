package server.game.pushing.paper.validator.transaction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.game.pushing.paper.bank.Bank;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CreateValidatorTests {
    protected CreateValidator createValidator;
    protected Bank bank;

    @BeforeEach
    protected void setUp() {
        bank = new Bank();
        createValidator = new CreateValidator(null, bank);
    }

    @Test
    protected void transaction_should_contain_the_transaction_type_create_as_the_first_argument() {
        assertFalse(createValidator.isTransactionValid(""));
        assertFalse(createValidator.isTransactionValid("nuke checking 00000000 0"));
        assertTrue(createValidator.isTransactionValid("create checking 00000000 0"));
    }

    @Test
    protected void transaction_should_contain_a_account_type_as_the_second_argument() {
        assertFalse(createValidator.isTransactionValid("create"));
        assertFalse(createValidator.isTransactionValid("create  00000001 0.1"));
        assertFalse(createValidator.isTransactionValid("create g68G*(^ 00000001 0.1"));
        assertTrue(createValidator.isTransactionValid("create checking 00000000 0"));
        assertTrue(createValidator.isTransactionValid("create savings 00000001 0.1"));
        assertTrue(createValidator.isTransactionValid("create cd 00000010 0.2 1000"));
    }

    @Test
    protected void transaction_should_contain_an_unique_8_digit_id_as_the_third_argument() {
        bank.createSavings("00000000", 0);
        assertFalse(createValidator.isTransactionValid("create savings 00000000 0.1"));

        assertFalse(createValidator.isTransactionValid("create savings"));
        assertFalse(createValidator.isTransactionValid("create savings  0.1"));

        assertFalse(createValidator.isTransactionValid("create savings 48 0.1"));

        assertFalse(createValidator.isTransactionValid("create savings 7834972 0.1"));
        assertTrue(createValidator.isTransactionValid("create savings 05793729 0.1"));
        assertFalse(createValidator.isTransactionValid("create savings 783447992 0.1"));

        assertFalse(createValidator.isTransactionValid("create savings 973957845729385729375 0.1"));

        assertFalse(createValidator.isTransactionValid("create savings 8G73mU*) 0.1"));
    }

    @Test
    protected void transaction_should_contain_an_apr_between_0_and_10_inclusive_as_the_fourth_argument() {
        assertFalse(createValidator.isTransactionValid("create cd 00000000  1000"));
        assertFalse(createValidator.isTransactionValid("create cd 00000000 78g& 1000"));


        assertFalse(createValidator.isTransactionValid("create cd 00000000 -10 1000"));

        assertFalse(createValidator.isTransactionValid("create cd 00000000 -1 1000"));
        assertTrue(createValidator.isTransactionValid("create cd 00000000 0 1000"));
        assertTrue(createValidator.isTransactionValid("create cd 00000000 1 1000"));

        assertTrue(createValidator.isTransactionValid("create cd 00000000 5 1000"));
        assertTrue(createValidator.isTransactionValid("create cd 00000000 6 1000"));

        assertTrue(createValidator.isTransactionValid("create cd 00000000 9 1000"));
        assertTrue(createValidator.isTransactionValid("create cd 00000000 10 1000"));
        assertFalse(createValidator.isTransactionValid("create cd 00000000 11 1000"));

        assertFalse(createValidator.isTransactionValid("create cd 00000000 20 1000"));
    }

    @Test
    protected void transaction_when_account_type_is_cd_should_contain_an_initial_balance_between_1000_and_10000_inclusive_as_the_fifth_argument() {
        assertFalse(createValidator.isTransactionValid("create cd 00000000 0 g78*(uU"));


        assertFalse(createValidator.isTransactionValid("create cd 00000000 0 -20000"));
        assertFalse(createValidator.isTransactionValid("create cd 00000000 0 0"));


        assertFalse(createValidator.isTransactionValid("create cd 00000000 0 100"));

        assertFalse(createValidator.isTransactionValid("create cd 00000000 0 900"));
        assertTrue(createValidator.isTransactionValid("create cd 00000000 0 1000"));
        assertTrue(createValidator.isTransactionValid("create cd 00000000 0 1100"));

        assertTrue(createValidator.isTransactionValid("create cd 00000000 0 5000"));
        assertTrue(createValidator.isTransactionValid("create cd 00000000 0 6000"));

        assertTrue(createValidator.isTransactionValid("create cd 00000000 0 9000"));
        assertTrue(createValidator.isTransactionValid("create cd 00000000 0 10000"));
        assertFalse(createValidator.isTransactionValid("create cd 00000000 0 11000"));

        assertFalse(createValidator.isTransactionValid("create cd 00000000 0 20000"));
    }

    @Test
    protected void transaction_should_be_case_insensitive() {
        assertTrue(createValidator.isTransactionValid("CrEaTe chECkIng 00000000 0"));
        assertTrue(createValidator.isTransactionValid("create saVINgs 00000001 0.1"));
        assertTrue(createValidator.isTransactionValid("creATe cd 00000010 0.2 1000"));
    }

    @Test
    protected void transaction_should_be_possible_with_useless_additional_arguments() {
        assertTrue(createValidator.isTransactionValid("create checking 00000000 0 0 0 0 0 0 0 0 0"));
        assertTrue(createValidator.isTransactionValid("create savings 00000001 0.1 nuke now"));
        assertTrue(createValidator.isTransactionValid("create cd 00000010 0.2 1000 g7^G*8"));
    }
}