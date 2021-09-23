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
    protected void create_validator_when_transaction_is_not_valid_should_pass_transaction_down_the_chain_of_responsibility() {
        bank.createChecking(id1, apr);
        double depositAmount = bank.getAccount(id1).getMaxDepositAmount();
        double withdrawAmount = bank.getAccount(id1).getMaxWithdrawAmount();

        validator.setNext(new DepositValidator(bank));

        assertTrue(validator.handle(String.format("%s %s %s", TransactionType.Deposit, id1, depositAmount)));
        assertFalse(validator.handle(String.format("%s %s %s", TransactionType.Withdraw, id1, withdrawAmount)));
    }

    @Test
    protected void transaction_should_contain_the_transaction_type_create_as_the_first_argument() {
        AccountType accountType = AccountType.Checking;

        assertFalse(validator.handle(""));
        assertFalse(validator.handle(String.format("%s %s %s %s", "", "", "", "")));
        assertFalse(validator.handle(String.format("%s %s %s %s", "", accountType, id1, apr)));
        assertFalse(validator.handle(String.format("%s %s %s %s", "nuke", accountType, id1, apr)));
        assertTrue(validator.handle(String.format("%s %s %s %s", transactionType, accountType, id1, apr)));
    }

    @Test
    protected void transaction_should_contain_a_account_type_as_the_second_argument() {
        assertFalse(validator.handle(String.format("%s %s %s %s", transactionType, "", "", "")));
        assertFalse(validator.handle(String.format("%s %s %s %s", transactionType, "", id1, apr)));
        assertFalse(validator.handle(String.format("%s %s %s %s", transactionType, "the power of friendship", id1, apr)));
        assertTrue(validator.handle(String.format("%s %s %s %s", transactionType, AccountType.Checking, id1, apr)));
        assertTrue(validator.handle(String.format("%s %s %s %s", transactionType, AccountType.Savings, id1, apr)));
        assertTrue(validator.handle(String.format("%s %s %s %s %s", transactionType, AccountType.CD, id1, apr, initialCDBalance)));
    }

    @Test
    protected void transaction_should_contain_an_unique_8_digit_id_as_the_third_argument() {
        AccountType accountType = AccountType.Savings;
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
    protected void transaction_should_contain_an_apr_between_0_and_10_inclusive_as_the_fourth_argument() {
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
    protected void transaction_when_account_type_is_cd_should_contain_an_initial_balance_between_1000_and_10000_inclusive_as_the_fifth_argument() {
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
    protected void transaction_should_be_case_insensitive() {
        assertTrue(validator.handle(String.format("%s %s %s %s", "crEaTe", "checking", id0, apr)));
        assertTrue(validator.handle(String.format("%s %s %s %s", "create", "saVINgs", id0, apr)));
        assertTrue(validator.handle(String.format("%s %s %s %s %s", "creATe", "Cd", id0, apr, initialCDBalance)));
    }

    @Test
    protected void transaction_should_be_possible_with_useless_additional_arguments() {
        assertTrue(validator.handle(String.format("%s %s %s %s %s", transactionType, AccountType.Checking, id0, apr, "the")));
        assertTrue(validator.handle(String.format("%s %s %s %s %s %s %s  %s", transactionType, AccountType.Savings, id0, apr, "power", "of", "friendship", id0)));
        assertTrue(validator.handle(String.format("%s %s %s %s %s  $s    $s     %s %s    ", transactionType, AccountType.CD, id0, apr, initialCDBalance, AccountType.CD, id0)));
    }
}
