package server.game.pushing.paper.store.chain_of_responsibility.transaction_validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.game.pushing.paper.store.bank.Bank;
import server.game.pushing.paper.store.bank.account.AccountType;
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
    private double apr;
    private double initialCDBalance;

    @BeforeEach
    protected void setUp() {
        bank = new Bank();
        validator = new CreateValidator(bank);

        transactionType = validator.getTransactionType();
        apr = bank.getMaxAPR();
        initialCDBalance = bank.getMinInitialCDBalance();
    }

    @Test
    protected void the_first_argument_in_create_transactions_is_the_transaction_type_create() {
        AccountType accountType = AccountType.CHECKING;

        assertFalse(validator.handle(""));
        assertFalse(validator.handle(String.format("%s %s %s %s", "", "", "", "")));
        assertFalse(validator.handle(String.format("%s %s %s %s", "", accountType, id1, apr)));
        assertFalse(validator.handle(String.format("%s %s %s %s", "nuke", accountType, id1, apr)));
        assertTrue(validator.handle(String.format("%s %s %s %s", transactionType, accountType, id1, apr)));
    }

    @Test
    protected void the_second_argument_in_create_transactions_an_account_type() {
        assertFalse(validator.handle(String.format("%s %s %s %s", transactionType, "", "", "")));
        assertFalse(validator.handle(String.format("%s %s %s %s", transactionType, "", id1, apr)));
        assertFalse(validator.handle(String.format("%s %s %s %s", transactionType, "the power of friendship", id1, apr)));
        assertTrue(validator.handle(String.format("%s %s %s %s", transactionType, AccountType.CHECKING, id1, apr)));
        assertTrue(validator.handle(String.format("%s %s %s %s", transactionType, AccountType.SAVINGS, id1, apr)));
        assertTrue(validator.handle(String.format("%s %s %s %s %s", transactionType, AccountType.CD, id1, apr, initialCDBalance)));
    }

    @Test
    protected void the_third_argument_in_create_transactions_is_a_unique_8_digit_id() {
        AccountType accountType = AccountType.SAVINGS;
        bank.createSavings(id0, apr);

        assertFalse(validator.handle(String.format("%s %s %s %s", transactionType, accountType, id0, apr)));

        assertFalse(validator.handle(String.format("%s %s %s %s", transactionType, accountType, "", "")));
        assertFalse(validator.handle(String.format("%s %s %s %s", transactionType, accountType, "", apr)));
        assertFalse(validator.handle(String.format("%s %s %s %s", transactionType, accountType, "4", apr)));

        assertFalse(validator.handle(String.format("%s %s %s %s", transactionType, accountType, "3rud8&*", apr)));
        assertFalse(validator.handle(String.format("%s %s %s %s", transactionType, accountType, "34782479H", apr)));
        assertFalse(validator.handle(String.format("%s %s %s %s", transactionType, accountType, "78y*gT^Y78y*gT^Y", apr)));

        assertFalse(validator.handle(String.format("%s %s %s %s", transactionType, accountType, "welcomes", apr)));
        assertFalse(validator.handle(String.format("%s %s %s %s", transactionType, accountType, "POWERFUL", apr)));
        assertFalse(validator.handle(String.format("%s %s %s %s", transactionType, accountType, "&G*(834t", apr)));
        assertTrue(validator.handle(String.format("%s %s %s %s", transactionType, accountType, id1, apr)));
    }

    @Test
    protected void the_fourth_argument_in_create_transactions_is_an_apr_between_0_and_10_inclusive() {
        AccountType accountType = AccountType.CD;

        assertFalse(validator.handle(String.format("%s %s %s %s %s", transactionType, accountType, id0, "", "")));
        assertFalse(validator.handle(String.format("%s %s %s %s %s", transactionType, accountType, id0, "", initialCDBalance)));
        assertFalse(validator.handle(String.format("%s %s %s %s %s", transactionType, accountType, id0, "f6F&", initialCDBalance)));

        assertFalse(validator.handle(String.format("%s %s %s %s %s", transactionType, accountType, id0, -20, initialCDBalance)));
        assertFalse(validator.handle(String.format("%s %s %s %s %s", transactionType, accountType, id0, -1, initialCDBalance)));
        assertTrue(validator.handle(String.format("%s %s %s %s %s", transactionType, accountType, id0, 0, initialCDBalance)));
        assertTrue(validator.handle(String.format("%s %s %s %s %s", transactionType, accountType, id0, 1, initialCDBalance)));
        assertTrue(validator.handle(String.format("%s %s %s %s %s", transactionType, accountType, id0, 5, initialCDBalance)));

        assertTrue(validator.handle(String.format("%s %s %s %s %s", transactionType, accountType, id0, 6, initialCDBalance)));
        assertTrue(validator.handle(String.format("%s %s %s %s %s", transactionType, accountType, id0, 9, initialCDBalance)));
        assertTrue(validator.handle(String.format("%s %s %s %s %s", transactionType, accountType, id0, 10, initialCDBalance)));
        assertFalse(validator.handle(String.format("%s %s %s %s %s", transactionType, accountType, id0, 11, initialCDBalance)));
        assertFalse(validator.handle(String.format("%s %s %s %s %s", transactionType, accountType, id0, 20, initialCDBalance)));
    }

    @Test
    protected void the_fifth_argument_in_create_cd_transactions_is_an_initial_balance_between_1000_and_10000_inclusive() {
        AccountType accountType = AccountType.CD;

        assertFalse(validator.handle(String.format("%s %s %s %s %s", transactionType, accountType, id0, apr, "")));
        assertFalse(validator.handle(String.format("%s %s %s %s %s", transactionType, accountType, id0, apr, "3fg78G&*")));

        assertFalse(validator.handle(String.format("%s %s %s %s %s", transactionType, accountType, id0, apr, 500)));
        assertFalse(validator.handle(String.format("%s %s %s %s %s", transactionType, accountType, id0, apr, 950)));
        assertTrue(validator.handle(String.format("%s %s %s %s %s", transactionType, accountType, id0, apr, 1000)));
        assertTrue(validator.handle(String.format("%s %s %s %s %s", transactionType, accountType, id0, apr, 1050)));
        assertTrue(validator.handle(String.format("%s %s %s %s %s", transactionType, accountType, id0, apr, 5000)));

        assertTrue(validator.handle(String.format("%s %s %s %s %s", transactionType, accountType, id0, apr, 6000)));
        assertTrue(validator.handle(String.format("%s %s %s %s %s", transactionType, accountType, id0, apr, 9500)));
        assertTrue(validator.handle(String.format("%s %s %s %s %s", transactionType, accountType, id0, apr, 10000)));
        assertFalse(validator.handle(String.format("%s %s %s %s %s", transactionType, accountType, id0, apr, 10500)));
        assertFalse(validator.handle(String.format("%s %s %s %s %s", transactionType, accountType, id0, apr, 15000)));

        assertFalse(validator.handle(String.format("%s %s %s %s %s", transactionType, accountType, id0, apr, -1000)));
        assertFalse(validator.handle(String.format("%s %s %s %s %s", transactionType, accountType, id0, apr, 0)));
    }

    @Test
    protected void create_validators_can_ignore_additional_arguments() {
        assertTrue(validator.handle(String.format("%s %s %s %s %s", transactionType, AccountType.CHECKING, id0, apr, "the")));
        assertTrue(validator.handle(String.format("%s %s %s %s %s %s %s  %s", transactionType, AccountType.SAVINGS, id0, apr, "power", "of", "friendship", id0)));
        assertTrue(validator.handle(String.format("%s %s %s %s %s  $s    $s     %s %s    ", transactionType, AccountType.CD, id0, apr, initialCDBalance, AccountType.CD, id0)));
    }

    @Test
    protected void create_validators_are_case_insensitive() {
        assertTrue(validator.handle(String.format("%s %s %s %s", "crEaTe", "checking", id0, apr)));
        assertTrue(validator.handle(String.format("%s %s %s %s", "create", "saVINgs", id0, apr)));
        assertTrue(validator.handle(String.format("%s %s %s %s %s", "creATe", "Cd", id0, apr, initialCDBalance)));
    }

    @Test
    protected void create_validators_can_be_in_a_chain_of_responsibility() {
        bank.createChecking(id1, apr);
        double depositAmount = bank.getAccount(id1).getMaxDepositAmount();
        double withdrawAmount = bank.getAccount(id1).getMaxWithdrawAmount();

        validator.setNext(new DepositValidator(bank));

        assertTrue(validator.handle(String.format("%s %s %s", TransactionType.Deposit, id1, depositAmount)));
        assertFalse(validator.handle(String.format("%s %s %s", TransactionType.Withdraw, id1, withdrawAmount)));
    }
}
