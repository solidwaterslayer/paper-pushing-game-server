package server.game.pushing.paper.store.chain_of_responsibility.transaction_validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.game.pushing.paper.store.bank.Bank;
import server.game.pushing.paper.store.bank.AccountType;
import server.game.pushing.paper.store.chain_of_responsibility.ChainOfResponsibility;
import server.game.pushing.paper.store.chain_of_responsibility.TransactionType;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CreateValidatorTests {
    private Bank bank;
    private ChainOfResponsibility validator;

    private TransactionType transactionType;
    private final String id0 = "34783874";
    private final String id1 = "44783874";
    private double cdBalance;

    @BeforeEach
    protected void setUp() {
        bank = new Bank();
        validator = new CreateValidator(bank);

        transactionType = validator.getTransactionType();
        cdBalance = bank.getMinCDBalance();
    }

    @Test
    protected void the_first_argument_in_create_transactions_is_the_transaction_type_create() {
        AccountType accountType = AccountType.Checking;

        assertFalse(validator.handle(""));
        assertFalse(validator.handle(String.format("%s %s %s", "", "", "")));
        assertFalse(validator.handle(String.format("%s %s %s", "", accountType, id1)));
        assertFalse(validator.handle(String.format("%s %s %s", "nuke", accountType, id1)));
        assertTrue(validator.handle(String.format("%s %s %s", transactionType, accountType, id1)));
    }

    @Test
    protected void the_second_argument_in_create_transactions_an_account_type() {
        assertFalse(validator.handle(String.format("%s %s %s", transactionType, "", "")));
        assertFalse(validator.handle(String.format("%s %s %s", transactionType, "", id1)));
        assertFalse(validator.handle(String.format("%s %s %s", transactionType, "the power of friendship", id1)));
        assertTrue(validator.handle(String.format("%s %s %s", transactionType, AccountType.Checking, id1)));
        assertTrue(validator.handle(String.format("%s %s %s", transactionType, AccountType.Savings, id1)));
        assertTrue(validator.handle(String.format("%s %s %s %s", transactionType, AccountType.CD, id1, cdBalance)));
    }

    @Test
    protected void the_third_argument_in_create_transactions_is_a_unique_8_digit_id() {
        AccountType accountType = AccountType.Savings;
        bank.createSavingsAccount(id0);

        assertFalse(validator.handle(String.format("%s %s %s", transactionType, accountType, id0)));

        assertFalse(validator.handle(String.format("%s %s %s", transactionType, accountType, "")));
        assertFalse(validator.handle(String.format("%s %s %s", transactionType, accountType, "")));
        assertFalse(validator.handle(String.format("%s %s %s", transactionType, accountType, "4")));

        assertFalse(validator.handle(String.format("%s %s %s", transactionType, accountType, "3rud8&*")));
        assertFalse(validator.handle(String.format("%s %s %s", transactionType, accountType, "34782479H")));
        assertFalse(validator.handle(String.format("%s %s %s", transactionType, accountType, "78y*gT^Y78y*gT^Y")));

        assertFalse(validator.handle(String.format("%s %s %s", transactionType, accountType, "welcomes")));
        assertFalse(validator.handle(String.format("%s %s %s", transactionType, accountType, "POWERFUL")));
        assertFalse(validator.handle(String.format("%s %s %s", transactionType, accountType, "&G*(834t")));
        assertTrue(validator.handle(String.format("%s %s %s", transactionType, accountType, id1)));
    }

    @Test
    protected void the_fourth_argument_in_create_cd_transactions_is_an_balance_between_1000_and_10000_inclusive() {
        AccountType accountType = AccountType.CD;

        assertFalse(validator.handle(String.format("%s %s %s %s", transactionType, accountType, id0, "")));
        assertFalse(validator.handle(String.format("%s %s %s %s", transactionType, accountType, id0, "3fg78G&*")));

        assertFalse(validator.handle(String.format("%s %s %s %s", transactionType, accountType, id0, 500)));
        assertFalse(validator.handle(String.format("%s %s %s %s", transactionType, accountType, id0, 950)));
        assertTrue(validator.handle(String.format("%s %s %s %s", transactionType, accountType, id0, 1000)));
        assertTrue(validator.handle(String.format("%s %s %s %s", transactionType, accountType, id0, 1050)));
        assertTrue(validator.handle(String.format("%s %s %s %s", transactionType, accountType, id0, 5000)));

        assertTrue(validator.handle(String.format("%s %s %s %s", transactionType, accountType, id0, 6000)));
        assertTrue(validator.handle(String.format("%s %s %s %s", transactionType, accountType, id0, 9500)));
        assertTrue(validator.handle(String.format("%s %s %s %s", transactionType, accountType, id0, 10000)));
        assertFalse(validator.handle(String.format("%s %s %s %s", transactionType, accountType, id0, 10500)));
        assertFalse(validator.handle(String.format("%s %s %s %s", transactionType, accountType, id0, 15000)));

        assertFalse(validator.handle(String.format("%s %s %s %s", transactionType, accountType, id0, -1000)));
        assertFalse(validator.handle(String.format("%s %s %s %s", transactionType, accountType, id0, 0)));
    }

    @Test
    protected void create_validators_can_ignore_additional_arguments() {
        assertTrue(validator.handle(String.format("%s %s %s %s", transactionType, AccountType.Checking, id0, "the")));
        assertTrue(validator.handle(String.format("%s %s %s %s %s %s  %s", transactionType, AccountType.Savings, id0, "power", "of", "friendship", id0)));
        assertTrue(validator.handle(String.format("%s %s %s %s  $s    $s     %s %s    ", transactionType, AccountType.CD, id0, cdBalance, AccountType.CD, id0)));
    }

    @Test
    protected void create_validators_are_case_insensitive() {
        assertTrue(validator.handle(String.format("%s %s %s", "crEaTe", "checking", id0)));
        assertTrue(validator.handle(String.format("%s %s %s", "create", "saVINgs", id0)));
        assertTrue(validator.handle(String.format("%s %s %s %s", "creATe", "Cd", id0, cdBalance)));
    }

    @Test
    protected void create_validators_can_be_in_a_chain_of_responsibility() {
        bank.createCheckingAccount(id1);
        double depositAmount = bank.getAccount(id1).getMaxDepositAmount();
        double withdrawAmount = bank.getAccount(id1).getMaxWithdrawAmount();

        validator.setNext(new DepositValidator(bank));

        assertTrue(validator.handle(String.format("%s %s %s", TransactionType.Deposit, id1, depositAmount)));
        assertFalse(validator.handle(String.format("%s %s %s", TransactionType.Withdraw, id1, withdrawAmount)));
    }
}
