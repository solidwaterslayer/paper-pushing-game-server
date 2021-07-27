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
        String transactionType0 = TransactionType.PassTime.split()[0];
        String transactionType1 = TransactionType.PassTime.split()[1];
        int months = getMonthsPerYear();

        assertFalse(passTimeValidator.handle(String.format("%s %s %s", "", "", "")));
        assertFalse(passTimeValidator.handle(String.format("%s %s %s", "", "", months)));
        assertFalse(passTimeValidator.handle(String.format("%s %s %s", "", transactionType1, months)));
        assertFalse(passTimeValidator.handle(String.format("%s %s %s", transactionType0, "", "")));
        assertFalse(passTimeValidator.handle(String.format("%s %s %s", transactionType0, "", months)));

        assertFalse(passTimeValidator.handle(String.format("%s %s %s", "transactionType0", transactionType1, months)));
        assertFalse(passTimeValidator.handle(String.format("%s %s %s", transactionType0, "dFJi", months)));
        assertTrue(passTimeValidator.handle(String.format("%s %s %s", transactionType0, transactionType1, months)));
    }

    @Test
    protected void transaction_should_contain_months_as_the_third_argument() {
        String transactionType0 = TransactionType.PassTime.split()[0];
        String transactionType1 = TransactionType.PassTime.split()[1];

        assertFalse(passTimeValidator.handle(String.format("%s %s %s", transactionType0, transactionType1, "")));
        assertFalse(passTimeValidator.handle(String.format("%s %s %s", transactionType0, transactionType1, "months")));
        assertTrue(passTimeValidator.handle(String.format("%s %s %s", transactionType0, transactionType1, getMonthsPerYear())));
    }

    @Test
    protected void transaction_should_contain_months_between_1_and_60_inclusive() {
        String transactionType0 = TransactionType.PassTime.split()[0];
        String transactionType1 = TransactionType.PassTime.split()[1];

        assertFalse(passTimeValidator.handle(String.format("%s %s %s", transactionType0, transactionType1, -60)));
        assertFalse(passTimeValidator.handle(String.format("%s %s %s", transactionType0, transactionType1, -3)));
        assertTrue(passTimeValidator.handle(String.format("%s %s %s", transactionType0, transactionType1, 1)));
        assertTrue(passTimeValidator.handle(String.format("%s %s %s", transactionType0, transactionType1, 5)));
        assertTrue(passTimeValidator.handle(String.format("%s %s %s", transactionType0, transactionType1, 30)));

        assertTrue(passTimeValidator.handle(String.format("%s %s %s", transactionType0, transactionType1, 40)));
        assertTrue(passTimeValidator.handle(String.format("%s %s %s", transactionType0, transactionType1, 56)));
        assertTrue(passTimeValidator.handle(String.format("%s %s %s", transactionType0, transactionType1, 60)));
        assertFalse(passTimeValidator.handle(String.format("%s %s %s", transactionType0, transactionType1, 64)));
        assertFalse(passTimeValidator.handle(String.format("%s %s %s", transactionType0, transactionType1, 120)));
    }

    @Test
    protected void transaction_should_be_case_insensitive() {
        int months = getMonthsPerYear();

        assertTrue(passTimeValidator.handle(String.format("%s %s %s", "PaSs", "time", months)));
        assertTrue(passTimeValidator.handle(String.format("%s %s %s", "pass", "tIMe", months)));
    }

    @Test
    protected void transaction_should_be_possible_with_useless_additional_arguments() {
        String transactionType0 = TransactionType.PassTime.split()[0];
        String transactionType1 = TransactionType.PassTime.split()[1];
        int months = getMonthsPerYear();

        assertTrue(passTimeValidator.handle(String.format("%s %s %s 0", transactionType0, transactionType1, months)));
        assertTrue(passTimeValidator.handle(String.format("%s %s %s 89 23892398 92839233 23", transactionType0, transactionType1, months)));
    }
}
