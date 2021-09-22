package server.game.pushing.paper.store.chain_of_responsibility.transaction_validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.game.pushing.paper.store.bank.Bank;
import server.game.pushing.paper.store.bank.account.AccountType;
import server.game.pushing.paper.store.chain_of_responsibility.ChainOfResponsibility;
import server.game.pushing.paper.store.chain_of_responsibility.TransactionType;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static server.game.pushing.paper.store.bank.Bank.getMonthsPerYear;

public class PassTimeValidatorTests {
    private Bank bank;
    private ChainOfResponsibility validator;

    private final int MONTHS = getMonthsPerYear();
    private TransactionType transactionType;

    @BeforeEach
    protected void setUp() {
        bank = new Bank();
        validator = new PassTimeValidator(bank);

        transactionType = validator.getTransactionType();
    }

    @Test
    protected void pass_time_validator_when_transaction_is_not_valid_should_pass_transaction_down_the_chain_of_responsibility() {
        AccountType accountType = AccountType.Savings;
        String id = "97439742";
        double apr = bank.getMaxAPR();
        validator.setNext(new CreateValidator(bank));
        assertTrue(validator.handle(String.format("%s %s %s %s", TransactionType.Create, accountType, id, apr)));
        bank.createSavings(id, apr);
        double depositAmount = bank.getAccount(id).getMaxDepositAmount();

        assertFalse(validator.handle(String.format("%s %s %s", TransactionType.Deposit, id, depositAmount)));
    }

    @Test
    protected void transaction_should_contain_the_transaction_type_pass_time_as_the_first_and_second_argument() {
        assertFalse(validator.handle(String.format("%s %s", "", "")));
        assertFalse(validator.handle(String.format("%s %s", "", MONTHS)));
        assertFalse(validator.handle(String.format("%s %s", "yes no", MONTHS)));
        assertTrue(validator.handle(String.format("%s %s", transactionType, MONTHS)));
    }

    @Test
    protected void transaction_should_contain_months_as_the_third_argument() {
        assertFalse(validator.handle(String.format("%s %s", transactionType, "")));
        assertFalse(validator.handle(String.format("%s %s", transactionType, "months")));
        assertTrue(validator.handle(String.format("%s %s", transactionType, getMonthsPerYear())));
    }

    @Test
    protected void transaction_should_contain_months_between_1_and_60_inclusive() {
        assertFalse(validator.handle(String.format("%s %s", transactionType, -60)));
        assertFalse(validator.handle(String.format("%s %s", transactionType, -3)));
        assertTrue(validator.handle(String.format("%s %s", transactionType, 1)));
        assertTrue(validator.handle(String.format("%s %s", transactionType, 5)));
        assertTrue(validator.handle(String.format("%s %s", transactionType, 30)));

        assertTrue(validator.handle(String.format("%s %s", transactionType, 40)));
        assertTrue(validator.handle(String.format("%s %s", transactionType, 56)));
        assertTrue(validator.handle(String.format("%s %s", transactionType, 60)));
        assertFalse(validator.handle(String.format("%s %s", transactionType, 64)));
        assertFalse(validator.handle(String.format("%s %s", transactionType, 120)));
    }

    @Test
    protected void transaction_should_be_case_insensitive() {
        assertTrue(validator.handle(String.format("%s %s", "PaSs tIme", MONTHS)));
    }

    @Test
    protected void transaction_should_be_possible_with_useless_additional_arguments() {
        assertTrue(validator.handle(String.format("%s %s %s", transactionType, MONTHS, "0")));
        assertTrue(validator.handle(String.format("%s %s %s %s %s %s", transactionType, MONTHS, 89, 23892398, 92839233, 23)));
    }
}
