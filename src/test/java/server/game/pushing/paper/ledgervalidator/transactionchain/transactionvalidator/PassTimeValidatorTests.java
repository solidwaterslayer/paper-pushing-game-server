package server.game.pushing.paper.ledgervalidator.transactionchain.transactionvalidator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.game.pushing.paper.ledgervalidator.bank.Bank;
import server.game.pushing.paper.ledgervalidator.bank.account.AccountType;
import server.game.pushing.paper.ledgervalidator.bank.account.Savings;
import server.game.pushing.paper.ledgervalidator.transactionchain.TransactionType;

import static org.junit.jupiter.api.Assertions.*;
import static server.game.pushing.paper.ledgervalidator.bank.Bank.*;

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
        String savingsID = "98478932";
        double apr = getMaxAPR();

        bank.createSavings(savingsID, apr);

        assertTrue(passTimeValidator.handle(String.format("%s %s %s %s %s", TransactionType.Create, AccountType.CD, "78437942", apr, getMinInitialCDBalance())));
        assertFalse(passTimeValidator.handle(String.format("%s %s %s", TransactionType.Deposit, savingsID, Savings.getMaxDepositAmount())));
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
        TransactionType transactionType = TransactionType.PassTime;
        int months = getMonthsPerYear();

        assertTrue(passTimeValidator.handle(String.format("%s %s 0", transactionType, months)));
        assertTrue(passTimeValidator.handle(String.format("%s %s 89 23892398 92839233 23", transactionType, months)));
    }
}
