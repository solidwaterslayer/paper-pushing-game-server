package server.game.pushing.paper.ledgervalidator.chainofresponsibility.transactionvalidator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.game.pushing.paper.ledgervalidator.bank.Bank;
import server.game.pushing.paper.ledgervalidator.bank.account.AccountType;
import server.game.pushing.paper.ledgervalidator.chainofresponsibility.ChainOfResponsibility;
import server.game.pushing.paper.ledgervalidator.chainofresponsibility.TransactionType;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static server.game.pushing.paper.ledgervalidator.bank.Bank.getMonthsPerYear;

public class PassTimeValidatorTests {
    protected Bank bank;
    protected PassTimeValidator passTimeValidator;

    @BeforeEach
    protected void setUp() {
        bank = new Bank();
        passTimeValidator = new PassTimeValidator(bank);
    }

    @Test
    protected void pass_time_validator_when_transaction_is_not_valid_should_pass_transaction_up_the_chain_of_responsibility() {
        passTimeValidator = (PassTimeValidator) ChainOfResponsibility.getInstance(Arrays.asList(passTimeValidator, new CreateValidator(bank), null));

        AccountType accountType = AccountType.Savings;
        String id0 = "97439742";
        String id1 = "98478932";
        double apr = bank.getMaxAPR();
        bank.createSavings(id0, apr);
        double depositAmount = bank.getAccount(id0).getMaxDepositAmount();

        assertTrue(passTimeValidator.handle(String.format("%s %s %s %s", TransactionType.Create, accountType, id1, apr)));
        assertFalse(passTimeValidator.handle(String.format("%s %s %s", TransactionType.Deposit, id1, depositAmount)));
    }

    @Test
    protected void transaction_should_contain_the_transaction_type_pass_time_as_the_first_and_second_argument() {
        TransactionType transactionType = TransactionType.PassTime;
        int months = getMonthsPerYear();

        assertFalse(passTimeValidator.handle(String.format("%s %s", "", "")));
        assertFalse(passTimeValidator.handle(String.format("%s %s", "", months)));
        assertFalse(passTimeValidator.handle(String.format("%s %s", "yes no", months)));
        assertTrue(passTimeValidator.handle(String.format("%s %s", transactionType, months)));
    }

    @Test
    protected void transaction_should_contain_months_as_the_third_argument() {
        TransactionType transactionType = TransactionType.PassTime;

        assertFalse(passTimeValidator.handle(String.format("%s %s", transactionType, "")));
        assertFalse(passTimeValidator.handle(String.format("%s %s", transactionType, "months")));
        assertTrue(passTimeValidator.handle(String.format("%s %s", transactionType, getMonthsPerYear())));
    }

    @Test
    protected void transaction_should_contain_months_between_1_and_60_inclusive() {
        TransactionType transactionType = TransactionType.PassTime;

        assertFalse(passTimeValidator.handle(String.format("%s %s", transactionType, -60)));
        assertFalse(passTimeValidator.handle(String.format("%s %s", transactionType, -3)));
        assertTrue(passTimeValidator.handle(String.format("%s %s", transactionType, 1)));
        assertTrue(passTimeValidator.handle(String.format("%s %s", transactionType, 5)));
        assertTrue(passTimeValidator.handle(String.format("%s %s", transactionType, 30)));

        assertTrue(passTimeValidator.handle(String.format("%s %s", transactionType, 40)));
        assertTrue(passTimeValidator.handle(String.format("%s %s", transactionType, 56)));
        assertTrue(passTimeValidator.handle(String.format("%s %s", transactionType, 60)));
        assertFalse(passTimeValidator.handle(String.format("%s %s", transactionType, 64)));
        assertFalse(passTimeValidator.handle(String.format("%s %s", transactionType, 120)));
    }

    @Test
    protected void transaction_should_be_case_insensitive() {
        int months = getMonthsPerYear();

        assertTrue(passTimeValidator.handle(String.format("%s %s", "PaSs tIme", months)));
    }

    @Test
    protected void transaction_should_be_possible_with_useless_additional_arguments() {
        int months = getMonthsPerYear();
        TransactionType transactionType = TransactionType.PassTime;

        assertTrue(passTimeValidator.handle(String.format("%s %s %s", transactionType, months, "0")));
        assertTrue(passTimeValidator.handle(String.format("%s %s %s %s %s %s", transactionType, months, 89, 23892398, 92839233, 23)));
    }
}
