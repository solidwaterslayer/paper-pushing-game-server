package server.game.pushing.paper.ledgervalidator.transactionchain.transactionprocessor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.game.pushing.paper.ledgervalidator.bank.Bank;
import server.game.pushing.paper.ledgervalidator.bank.account.AccountType;
import server.game.pushing.paper.ledgervalidator.transactionchain.TransactionType;

import static org.junit.jupiter.api.Assertions.*;

public class CreateProcessorTests {
    protected Bank bank;
    protected CreateProcessor createProcessor;

    @BeforeEach
    protected void setUp() {
        bank = new Bank();
        createProcessor = new CreateProcessor(bank);
    }

    @Test
    protected void create_processor_when_transaction_can_not_process_should_pass_transaction_up_the_chain_of_responsibility() {
        String id = "98430842";
        double apr = bank.getMaxAPR();
        bank.createSavings(id, apr);
        double depositAmount = bank.getAccount(id).getMaxDepositAmount();
        double withdrawAmount = bank.getAccount(id).getMaxWithdrawAmount();

        createProcessor.setNext(new DepositProcessor(bank));

        assertTrue(createProcessor.handle(String.format("%s %s %s", TransactionType.Deposit, id, depositAmount)));
        assertEquals(depositAmount, bank.getAccount(id).getBalance());
        assertFalse(createProcessor.handle(String.format("%s %s %s", TransactionType.Withdraw, id, withdrawAmount)));
    }

    @Test
    protected void create_checking_transaction_should_process() {
        TransactionType transactionType = TransactionType.Create;
        AccountType accountType = AccountType.Checking;
        String id = "87438743";
        double apr = bank.getMaxAPR();

        assertTrue(createProcessor.handle(String.format("%s %s %s %s", transactionType, accountType, id, apr)));
        assertEquals(accountType, bank.getAccount(id).getAccountType());
        assertEquals(id, bank.getAccount(id).getID());
        assertEquals(apr, bank.getAccount(id).getAPR());
        assertEquals(0, bank.getAccount(id).getBalance());
    }

    @Test
    protected void create_savings_transaction_should_process() {
        TransactionType transactionType = TransactionType.Create;
        AccountType accountType = AccountType.Savings;
        String id = "87438742";
        double apr = bank.getMaxAPR();

        assertTrue(createProcessor.handle(String.format("%s %s %s %s", transactionType, accountType, id, apr)));
        assertEquals(accountType, bank.getAccount(id).getAccountType());
        assertEquals(id, bank.getAccount(id).getID());
        assertEquals(apr, bank.getAccount(id).getAPR());
        assertEquals(0, bank.getAccount(id).getBalance());
    }

    @Test
    protected void create_cd_transaction_should_process() {
        TransactionType transactionType = TransactionType.Create;
        AccountType accountType = AccountType.CD;
        String id = "87438778";
        double apr = bank.getMaxAPR();
        double initialCDBalance = bank.getMinInitialCDBalance();

        assertTrue(createProcessor.handle(String.format("%s %s %s %s %s", transactionType, accountType, id, apr, initialCDBalance)));
        assertEquals(accountType, bank.getAccount(id).getAccountType());
        assertEquals(id, bank.getAccount(id).getID());
        assertEquals(apr, bank.getAccount(id).getAPR());
        assertEquals(initialCDBalance, bank.getAccount(id).getBalance());
    }

    @Test
    protected void transaction_should_be_case_insensitive() {
        assertEquals(AccountType.Checking, AccountType.valueOf("Checking"));
        String checkingID = "17349724";
        String savingsID = "27349724";
        String cdID = "87349724";
        double apr = bank.getMaxAPR();
        double initialCDBalance = bank.getMinInitialCDBalance();

        assertTrue(createProcessor.handle(String.format("%s %s %s %s", "crEaTe", "checking", checkingID, apr)));
        assertTrue(createProcessor.handle(String.format("%s %s %s %s", "create", "saVINgs", savingsID, apr)));
        assertTrue(createProcessor.handle(String.format("%s %s %s %s %s", "creATe", "Cd", cdID, apr, initialCDBalance)));
    }

    @Test
    protected void transaction_should_be_possible_with_useless_additional_arguments() {
        TransactionType transactionType = createProcessor.getTransactionType();
        String checkingID = "17349724";
        String savingsID = "27349724";
        String cdID = "87349724";
        double apr = bank.getMaxAPR();
        double initialCDBalance = bank.getMinInitialCDBalance();

        assertTrue(createProcessor.handle(String.format("%s %s %s %s %s", transactionType, AccountType.Checking, checkingID, apr, "0")));
        assertTrue(createProcessor.handle(String.format("%s %s %s %s %s %s %s %s", transactionType, AccountType.Savings, savingsID, apr, "nuke", AccountType.CD, "38ur", 34)));
        assertTrue(createProcessor.handle(String.format("%s %s %s %s %s  %s %s     %s %s    ", transactionType, AccountType.CD, cdID, apr, initialCDBalance, "8", 8, "eight", 4 + 4)));
    }
}
