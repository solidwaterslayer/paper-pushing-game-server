package server.game.pushing.paper.store.chainofresponsibility.transactionprocessor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.game.pushing.paper.store.bank.Bank;
import server.game.pushing.paper.store.bank.account.Account;
import server.game.pushing.paper.store.bank.account.AccountType;
import server.game.pushing.paper.store.chainofresponsibility.ChainOfResponsibility;
import server.game.pushing.paper.store.chainofresponsibility.TransactionType;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static server.game.pushing.paper.store.bank.Bank.getMonthsPerYear;
import static server.game.pushing.paper.store.bank.BankTests.passTime;

public class PassTimeProcessorTests {
    private Bank bank;
    private ChainOfResponsibility processor;

    private double minBalanceFee;
    private int months;
    private TransactionType transactionType;

    private final String CHECKING_ID = "98408842";
    private final String SAVINGS_ID = "89438042";
    private final String CD_ID = "98430842";
    private double apr;
    private double initialCDBalance;

    @BeforeEach
    protected void setUp() {
        bank = new Bank();
        processor = new PassTimeProcessor(bank);

        minBalanceFee = bank.getMinBalanceFee();
        months = getMonthsPerYear();
        transactionType = processor.getTransactionType();
        apr = bank.getMaxAPR();
        initialCDBalance = bank.getMinInitialCDBalance();

        bank.createChecking(CHECKING_ID, apr);
        bank.createSavings(SAVINGS_ID, apr);
        bank.createCD(CD_ID, apr, initialCDBalance);
    }

    @Test
    protected void pass_time_processor_when_transaction_can_not_process_should_pass_transaction_up_the_chain_of_responsibility() {
        processor = ChainOfResponsibility.getInstance(Arrays.asList(processor, new CreateProcessor(bank), null));

        String id0 = "10000010";
        String id1 = SAVINGS_ID;

        assertTrue(processor.handle(String.format("%s %s %s %s %s", TransactionType.Create, AccountType.CD, id0, apr, initialCDBalance)));
        Account account = bank.getAccount(id0);
        assertEquals(AccountType.CD, account.getAccountType());
        assertEquals(id0, account.getID());
        assertEquals(apr, account.getAPR());
        assertEquals(initialCDBalance, account.getBalance());
        assertFalse(processor.handle(String.format("%s %s %s", TransactionType.Deposit, id1, bank.getAccount(id1).getMaxDepositAmount())));
    }

    @Test
    protected void transaction_should_apply_apr() {
        double checkingDepositAmount = bank.getAccount(CHECKING_ID).getMaxDepositAmount();
        double savingsDepositAmount = bank.getAccount(SAVINGS_ID).getMaxDepositAmount();

        bank.deposit(CHECKING_ID, checkingDepositAmount);
        bank.deposit(SAVINGS_ID, savingsDepositAmount);

        assertTrue(processor.handle(String.format("%s %s", transactionType, months)));
        assertEquals(passTime(minBalanceFee, months, AccountType.Checking, apr, checkingDepositAmount), bank.getAccount(CHECKING_ID).getBalance());
        assertEquals(passTime(minBalanceFee, months, AccountType.Savings, apr, savingsDepositAmount), bank.getAccount(SAVINGS_ID).getBalance());
        assertEquals(passTime(minBalanceFee, months, AccountType.CD, apr, initialCDBalance), bank.getAccount(CD_ID).getBalance());
    }

    @Test
    protected void transaction_when_balance_is_less_than_or_equal_to_100_should_apply_min_balance_fee_then_apr() {
        months = 2;
        double checkingDepositAmount = 75;
        double savingsDepositAmount = 100;

        bank.deposit(CHECKING_ID, checkingDepositAmount);
        bank.deposit(SAVINGS_ID, savingsDepositAmount);

        assertTrue(processor.handle(String.format("%s %s", transactionType, months)));
        assertEquals(passTime(minBalanceFee, months, AccountType.Checking, apr, checkingDepositAmount), bank.getAccount(CHECKING_ID).getBalance());
        assertEquals(passTime(minBalanceFee, months, AccountType.Savings, apr, savingsDepositAmount), bank.getAccount(SAVINGS_ID).getBalance());
        assertEquals(passTime(minBalanceFee, months, AccountType.CD, apr, initialCDBalance), bank.getAccount(CD_ID).getBalance());
    }

    @Test
    protected void transaction_when_balance_is_0_should_remove_account() {
        months = 2;
        double depositAmount = 25;

        bank.deposit(SAVINGS_ID, depositAmount);

        assertTrue(processor.handle(String.format("%s %s", transactionType, months)));
        assertFalse(bank.containsAccount(CHECKING_ID));
        assertFalse(bank.containsAccount(SAVINGS_ID));
        assertTrue(bank.containsAccount(CD_ID));
        assertEquals(passTime(minBalanceFee, months, AccountType.CD, apr, initialCDBalance), bank.getAccount(CD_ID).getBalance());
    }

    @Test
    protected void transaction_should_be_case_insensitive() {
        assertTrue(processor.handle(String.format("%s %s", "PaSs tIme", months)));
    }

    @Test
    protected void transaction_should_be_possible_with_useless_additional_arguments() {
        assertTrue(processor.handle(String.format("%s %s %s", transactionType, months, "0")));
        assertTrue(processor.handle(String.format("%s %s %s %s %s %s", transactionType, months, 89, 23892398, 92839233, 23)));
    }
}
