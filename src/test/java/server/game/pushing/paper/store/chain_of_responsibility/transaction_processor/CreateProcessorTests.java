package server.game.pushing.paper.store.chain_of_responsibility.transaction_processor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.game.pushing.paper.store.bank.Bank;
import server.game.pushing.paper.store.bank.account.Account;
import server.game.pushing.paper.store.bank.AccountType;
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
    private double cdBalance;

    @BeforeEach
    protected void setUp() {
        bank = new Bank();
        processor = new CreateProcessor(bank);

        transactionType = processor.getTransactionType();
        cdBalance = bank.getMinCDBalance();
    }

    @Test
    protected void create_processors_can_create_checking_accounts() {
        AccountType accountType = AccountType.Checking;
        String id = CHECKING_ID;

        assertTrue(processor.handle(String.format("%s %s %s", transactionType, accountType, id)));

        Account account = bank.getAccount(id);
        assertEquals(accountType, account.getAccountType());
        assertEquals(id, account.getID());
        assertEquals(0, account.getBalance());
    }

    @Test
    protected void create_processors_can_create_savings_accounts() {
        AccountType accountType = AccountType.Savings;
        String id = SAVINGS_ID;

        assertTrue(processor.handle(String.format("%s %s %s", transactionType, accountType, id)));

        Account account = bank.getAccount(id);
        assertEquals(accountType, account.getAccountType());
        assertEquals(id, account.getID());
        assertEquals(0, account.getBalance());
    }

    @Test
    protected void create_processors_can_create_cd_accounts() {
        AccountType accountType = AccountType.CD;
        String id = CD_ID;

        assertTrue(processor.handle(String.format("%s %s %s %s", transactionType, accountType, id, cdBalance)));

        Account account = bank.getAccount(id);
        assertEquals(accountType, account.getAccountType());
        assertEquals(id, account.getID());
        assertEquals(cdBalance, account.getBalance());
    }

    @Test
    protected void create_processors_can_ignore_additional_arguments() {
        assertTrue(processor.handle(String.format("%s %s %s %s", transactionType, AccountType.Checking, CHECKING_ID, "0")));
        assertTrue(processor.handle(String.format("%s %s %s %s %s %s %s", transactionType, AccountType.Savings, SAVINGS_ID, "nuke", AccountType.CD, "38ur", 34)));
        assertTrue(processor.handle(String.format("%s %s %s %s  %s %s     %s %s    ", transactionType, AccountType.CD, CD_ID, cdBalance, "8", 8, "eight", 4 + 4)));
    }

    @Test
    protected void create_processors_are_case_insensitive() {
        assertTrue(processor.handle(String.format("%s %s %s", "crEaTe", "checking", CHECKING_ID)));
        assertTrue(processor.handle(String.format("%s %s %s", "create", "saVINgs", SAVINGS_ID)));
        assertTrue(processor.handle(String.format("%s %s %s %s", "creATe", "Cd", CD_ID, cdBalance)));
    }

    @Test
    protected void create_processors_can_be_in_a_chain_of_responsibility() {
        AccountType accountType = AccountType.Savings;
        String id = SAVINGS_ID;
        assertTrue(processor.handle(String.format("%s %s %s", transactionType, accountType, id)));
        double depositAmount = bank.getAccount(id).getMaxDepositAmount();
        double withdrawAmount = bank.getAccount(id).getMaxWithdrawAmount();

        processor.setNext(new DepositProcessor(bank));

        assertTrue(processor.handle(String.format("%s %s %s", TransactionType.Deposit, id, depositAmount)));
        assertEquals(depositAmount, bank.getAccount(id).getBalance());
        assertFalse(processor.handle(String.format("%s %s %s", TransactionType.Withdraw, id, withdrawAmount)));
    }
}
