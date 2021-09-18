package server.game.pushing.paper.ledgervalidator.transactionchain.transactionvalidator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.game.pushing.paper.ledgervalidator.bank.Bank;
import server.game.pushing.paper.ledgervalidator.bank.account.AccountType;
import server.game.pushing.paper.ledgervalidator.transactionchain.TransactionType;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CreateValidatorTests {
    protected Bank bank;
    protected CreateValidator createValidator;

    @BeforeEach
    protected void setUp() {
        bank = new Bank();
        createValidator = new CreateValidator(bank);
    }

    @Test
    protected void create_validator_when_transaction_is_not_valid_should_pass_transaction_up_the_chain_of_responsibility() {
        String id = "87439742";
        double apr = bank.getMaxAPR();
        bank.createChecking(id, apr);
        double depositAmount = bank.getAccount(id).getMaxDepositAmount();
        double withdrawAmount = bank.getAccount(id).getMaxWithdrawAmount();

        createValidator.setNext(new DepositValidator(bank));

        assertTrue(createValidator.handle(String.format("%s %s %s", TransactionType.Deposit, id, depositAmount)));
        assertFalse(createValidator.handle(String.format("%s %s %s", TransactionType.Withdraw, id, withdrawAmount)));
    }

    @Test
    protected void transaction_should_contain_the_transaction_type_create_as_the_first_argument() {
        TransactionType transactionType = createValidator.getTransactionType();
        AccountType accountType = AccountType.Checking;
        String id = "34783794";
        double apr = bank.getMaxAPR();

        assertFalse(createValidator.handle(String.format("%s %s %s %s", "", "", "", "")));
        assertFalse(createValidator.handle(String.format("%s %s %s %s", "", accountType, id, apr)));
        assertFalse(createValidator.handle(String.format("%s %s %s %s", "nuke", accountType, id, apr)));
        assertTrue(createValidator.handle(String.format("%s %s %s %s", transactionType, accountType, id, apr)));
    }

    @Test
    protected void transaction_should_contain_a_account_type_as_the_second_argument() {
        TransactionType transactionType = createValidator.getTransactionType();
        String id = "53795478";
        double apr = bank.getMaxAPR();
        double initialCDBalance = bank.getMinInitialCDBalance();

        assertFalse(createValidator.handle(String.format("%s %s %s %s", transactionType, "", "", "")));
        assertFalse(createValidator.handle(String.format("%s %s %s %s", transactionType, "", id, apr)));
        assertFalse(createValidator.handle(String.format("%s %s %s %s", transactionType, "the power of friendship", id, apr)));
        assertTrue(createValidator.handle(String.format("%s %s %s %s", transactionType, AccountType.Checking, id, apr)));
        assertTrue(createValidator.handle(String.format("%s %s %s %s", transactionType, AccountType.Savings, id, apr)));
        assertTrue(createValidator.handle(String.format("%s %s %s %s %s", transactionType, AccountType.CD, id, apr, initialCDBalance)));
    }

    @Test
    protected void transaction_should_contain_an_unique_8_digit_id_as_the_third_argument() {
        TransactionType transactionType = createValidator.getTransactionType();
        AccountType accountType = AccountType.Savings;
        String id = "34783874";
        double apr = bank.getMaxAPR();
        bank.createSavings(id, apr);

        assertFalse(createValidator.handle(String.format("%s %s %s %s", transactionType, accountType, id, apr)));

        assertFalse(createValidator.handle(String.format("%s %s %s %s", transactionType, accountType, "", "")));
        assertFalse(createValidator.handle(String.format("%s %s %s %s", transactionType, accountType, "", apr)));
        assertFalse(createValidator.handle(String.format("%s %s %s %s", transactionType, accountType, "4", apr)));

        assertFalse(createValidator.handle(String.format("%s %s %s %s", transactionType, accountType, "3rud8&*", apr)));
        assertFalse(createValidator.handle(String.format("%s %s %s %s", transactionType, accountType, "34782479H", apr)));
        assertFalse(createValidator.handle(String.format("%s %s %s %s", transactionType, accountType, "78y*gT^Y78y*gT^Y", apr)));

        assertFalse(createValidator.handle(String.format("%s %s %s %s", transactionType, accountType, "welcomes", apr)));
        assertFalse(createValidator.handle(String.format("%s %s %s %s", transactionType, accountType, "POWERFUL", apr)));
        assertFalse(createValidator.handle(String.format("%s %s %s %s", transactionType, accountType, "&G*(834t", apr)));
        assertTrue(createValidator.handle(String.format("%s %s %s %s", transactionType, accountType, "34789724", apr)));
    }

    @Test
    protected void transaction_should_contain_an_apr_between_0_and_10_inclusive_as_the_fourth_argument() {
        TransactionType transactionType = createValidator.getTransactionType();
        AccountType accountType = AccountType.CD;
        String id = "87349724";
        double initialCDBalance = bank.getMinInitialCDBalance();

        assertFalse(createValidator.handle(String.format("%s %s %s %s %s", transactionType, accountType, id, "", "")));
        assertFalse(createValidator.handle(String.format("%s %s %s %s %s", transactionType, accountType, id, "", initialCDBalance)));
        assertFalse(createValidator.handle(String.format("%s %s %s %s %s", transactionType, accountType, id, "f6F&", initialCDBalance)));

        assertFalse(createValidator.handle(String.format("%s %s %s %s %s", transactionType, accountType, id, -20, initialCDBalance)));
        assertFalse(createValidator.handle(String.format("%s %s %s %s %s", transactionType, accountType, id, -1, initialCDBalance)));
        assertTrue(createValidator.handle(String.format("%s %s %s %s %s", transactionType, accountType, id, 0, initialCDBalance)));
        assertTrue(createValidator.handle(String.format("%s %s %s %s %s", transactionType, accountType, id, 1, initialCDBalance)));
        assertTrue(createValidator.handle(String.format("%s %s %s %s %s", transactionType, accountType, id, 5, initialCDBalance)));

        assertTrue(createValidator.handle(String.format("%s %s %s %s %s", transactionType, accountType, id, 6, initialCDBalance)));
        assertTrue(createValidator.handle(String.format("%s %s %s %s %s", transactionType, accountType, id, 9, initialCDBalance)));
        assertTrue(createValidator.handle(String.format("%s %s %s %s %s", transactionType, accountType, id, 10, initialCDBalance)));
        assertFalse(createValidator.handle(String.format("%s %s %s %s %s", transactionType, accountType, id, 11, initialCDBalance)));
        assertFalse(createValidator.handle(String.format("%s %s %s %s %s", transactionType, accountType, id, 20, initialCDBalance)));
    }

    @Test
    protected void transaction_when_account_type_is_cd_should_contain_an_initial_balance_between_1000_and_10000_inclusive_as_the_fifth_argument() {
        TransactionType transactionType = createValidator.getTransactionType();
        AccountType accountType = AccountType.CD;
        String id = "87349724";
        double apr = bank.getMaxAPR();

        assertFalse(createValidator.handle(String.format("%s %s %s %s %s", transactionType, accountType, id, apr, "")));
        assertFalse(createValidator.handle(String.format("%s %s %s %s %s", transactionType, accountType, id, apr, "3fg78G&*")));

        assertFalse(createValidator.handle(String.format("%s %s %s %s %s", transactionType, accountType, id, apr, 500)));
        assertFalse(createValidator.handle(String.format("%s %s %s %s %s", transactionType, accountType, id, apr, 950)));
        assertTrue(createValidator.handle(String.format("%s %s %s %s %s", transactionType, accountType, id, apr, 1000)));
        assertTrue(createValidator.handle(String.format("%s %s %s %s %s", transactionType, accountType, id, apr, 1050)));
        assertTrue(createValidator.handle(String.format("%s %s %s %s %s", transactionType, accountType, id, apr, 5000)));

        assertTrue(createValidator.handle(String.format("%s %s %s %s %s", transactionType, accountType, id, apr, 6000)));
        assertTrue(createValidator.handle(String.format("%s %s %s %s %s", transactionType, accountType, id, apr, 9500)));
        assertTrue(createValidator.handle(String.format("%s %s %s %s %s", transactionType, accountType, id, apr, 10000)));
        assertFalse(createValidator.handle(String.format("%s %s %s %s %s", transactionType, accountType, id, apr, 10500)));
        assertFalse(createValidator.handle(String.format("%s %s %s %s %s", transactionType, accountType, id, apr, 15000)));

        assertFalse(createValidator.handle(String.format("%s %s %s %s %s", transactionType, accountType, id, apr, -1000)));
        assertFalse(createValidator.handle(String.format("%s %s %s %s %s", transactionType, accountType, id, apr, 0)));
    }

    @Test
    protected void transaction_should_be_case_insensitive() {
        String id = "87349724";
        double apr = bank.getMaxAPR();
        double initialCDBalance = bank.getMinInitialCDBalance();

        assertTrue(createValidator.handle(String.format("%s %s %s %s", "crEaTe", "checking", id, apr)));
        assertTrue(createValidator.handle(String.format("%s %s %s %s", "create", "saVINgs", id, apr)));
        assertTrue(createValidator.handle(String.format("%s %s %s %s %s", "creATe", "Cd", id, apr, initialCDBalance)));
    }

    @Test
    protected void transaction_should_be_possible_with_useless_additional_arguments() {
        TransactionType transactionType = createValidator.getTransactionType();
        String id = "14567893";
        double apr = bank.getMaxAPR();
        double initialCDBalance = bank.getMinInitialCDBalance();

        assertTrue(createValidator.handle(String.format("%s %s %s %s %s", transactionType, AccountType.Checking, id, apr, "the")));
        assertTrue(createValidator.handle(String.format("%s %s %s %s %s %s %s  %s", transactionType, AccountType.Savings, id, apr, "power", "of", "friendship", id)));
        assertTrue(createValidator.handle(String.format("%s %s %s %s %s  $s    $s     %s %s    ", transactionType, AccountType.CD, id, apr, initialCDBalance, AccountType.CD, id)));
    }
}
