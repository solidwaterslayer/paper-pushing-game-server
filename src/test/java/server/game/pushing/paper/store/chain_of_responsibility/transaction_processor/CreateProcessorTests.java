package server.game.pushing.paper.store.chain_of_responsibility.transaction_processor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.game.pushing.paper.store.bank.Bank;
import server.game.pushing.paper.store.bank.account.Account;
import server.game.pushing.paper.store.bank.account.AccountType;
import server.game.pushing.paper.store.chain_of_responsibility.ChainOfResponsibility;
import server.game.pushing.paper.store.chain_of_responsibility.TransactionType;

import static org.junit.jupiter.api.Assertions.*;

public class CreateProcessorTests {
    private Bank bank;
    private ChainOfResponsibility processor;

    private TransactionType transactionType;
    private final String CHECKING_ID = "17349724";
    private final String SAVINGS_ID = "27349724";
    private final String CD_ID = "87349724";
    private double apr;
    private double initialCDBalance;

    @BeforeEach
    protected void setUp() {
        bank = new Bank();
        processor = new CreateProcessor(bank);

        transactionType = processor.getTransactionType();
        apr = bank.getMaxAPR();
        initialCDBalance = bank.getMinInitialCDBalance();
    }

    @Test
    protected void create_processor_when_transaction_can_not_process_should_pass_transaction_down_the_chain_of_responsibility() {
        AccountType accountType = AccountType.Savings;
        String id = SAVINGS_ID;
        assertTrue(processor.handle(String.format("%s %s %s %s", transactionType, accountType, id, apr)));
        double depositAmount = bank.getAccount(id).getMaxDepositAmount();
        double withdrawAmount = bank.getAccount(id).getMaxWithdrawAmount();

        processor.setNext(new DepositProcessor(bank));

        assertTrue(processor.handle(String.format("%s %s %s", TransactionType.Deposit, id, depositAmount)));
        assertEquals(depositAmount, bank.getAccount(id).getBalance());
        assertFalse(processor.handle(String.format("%s %s %s", TransactionType.Withdraw, id, withdrawAmount)));
    }

    @Test
    protected void create_checking_transaction_should_process() {
        AccountType accountType = AccountType.Checking;
        String id = CHECKING_ID;

        assertTrue(processor.handle(String.format("%s %s %s %s", transactionType, accountType, id, apr)));

        Account account = bank.getAccount(id);
        assertEquals(accountType, account.getAccountType());
        assertEquals(id, account.getID());
        assertEquals(apr, account.getAPR());
        assertEquals(0, account.getBalance());
    }

    @Test
    protected void create_savings_transaction_should_process() {
        AccountType accountType = AccountType.Savings;
        String id = SAVINGS_ID;

        assertTrue(processor.handle(String.format("%s %s %s %s", transactionType, accountType, id, apr)));

        Account account = bank.getAccount(id);
        assertEquals(accountType, account.getAccountType());
        assertEquals(id, account.getID());
        assertEquals(apr, account.getAPR());
        assertEquals(0, account.getBalance());
    }

    @Test
    protected void create_cd_transaction_should_process() {
        AccountType accountType = AccountType.CD;
        String id = CD_ID;

        assertTrue(processor.handle(String.format("%s %s %s %s %s", transactionType, accountType, id, apr, initialCDBalance)));

        Account account = bank.getAccount(id);
        assertEquals(accountType, account.getAccountType());
        assertEquals(id, account.getID());
        assertEquals(apr, account.getAPR());
        assertEquals(initialCDBalance, account.getBalance());
    }

    @Test
    protected void transaction_should_be_case_insensitive() {
        assertTrue(processor.handle(String.format("%s %s %s %s", "crEaTe", "checking", CHECKING_ID, apr)));
        assertTrue(processor.handle(String.format("%s %s %s %s", "create", "saVINgs", SAVINGS_ID, apr)));
        assertTrue(processor.handle(String.format("%s %s %s %s %s", "creATe", "Cd", CD_ID, apr, initialCDBalance)));
    }

    @Test
    protected void transaction_should_be_possible_with_useless_additional_arguments() {
        assertTrue(processor.handle(String.format("%s %s %s %s %s", transactionType, AccountType.Checking, CHECKING_ID, apr, "0")));
        assertTrue(processor.handle(String.format("%s %s %s %s %s %s %s %s", transactionType, AccountType.Savings, SAVINGS_ID, apr, "nuke", AccountType.CD, "38ur", 34)));
        assertTrue(processor.handle(String.format("%s %s %s %s %s  %s %s     %s %s    ", transactionType, AccountType.CD, CD_ID, apr, initialCDBalance, "8", 8, "eight", 4 + 4)));
    }
}
