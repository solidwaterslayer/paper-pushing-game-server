package server.game.pushing.paper.ledgervalidator.chainofresponsibility.transactionprocessor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.game.pushing.paper.ledgervalidator.bank.Bank;
import server.game.pushing.paper.ledgervalidator.bank.account.AccountType;
import server.game.pushing.paper.ledgervalidator.chainofresponsibility.TransactionType;

import static org.junit.jupiter.api.Assertions.*;
import static server.game.pushing.paper.ledgervalidator.bank.Bank.getMonthsPerYear;
import static server.game.pushing.paper.ledgervalidator.bank.BankTests.passTime;

public class PassTimeProcessorTests {
    protected Bank bank;
    protected PassTimeProcessor passTimeProcessor;

    protected final String CHECKING_ID = "98408842";
    protected final String SAVINGS_ID = "89438042";
    protected final String CD_ID = "98430842";
    protected double apr;
    protected double initialCDBalance;

    @BeforeEach
    protected void setUp() {
        bank = new Bank();
        passTimeProcessor = new PassTimeProcessor(bank);

        apr = bank.getMaxAPR();
        initialCDBalance = bank.getMinInitialCDBalance();

        bank.createChecking(CHECKING_ID, apr);
        bank.createSavings(SAVINGS_ID, apr);
        bank.createCD(CD_ID, apr, initialCDBalance);
    }

    @Test
    protected void pass_time_processor_when_transaction_can_not_process_should_pass_transaction_up_the_chain_of_responsibility() {
        String id0 = "10000010";
        String id1 = SAVINGS_ID;

        passTimeProcessor.setNext(new CreateProcessor(bank));

        assertTrue(passTimeProcessor.handle(String.format("%s %s %s %s %s", TransactionType.Create, AccountType.CD, id0, apr, initialCDBalance)));
        assertEquals(AccountType.CD, bank.getAccount(id0).getAccountType());
        assertEquals(id0, bank.getAccount(id0).getID());
        assertEquals(apr, bank.getAccount(id0).getAPR());
        assertEquals(initialCDBalance, bank.getAccount(id0).getBalance());
        assertFalse(passTimeProcessor.handle(String.format("%s %s %s", TransactionType.Deposit, id1, bank.getAccount(id1).getMaxDepositAmount())));
    }

    @Test
    protected void transaction_should_apply_apr() {
        double minBalanceFee = bank.getMinBalanceFee();
        int months = getMonthsPerYear();
        TransactionType transactionType = TransactionType.PassTime;
        double checkingDepositAmount = bank.getAccount(CHECKING_ID).getMaxDepositAmount();
        double savingsDepositAmount = bank.getAccount(SAVINGS_ID).getMaxDepositAmount();

        bank.deposit(CHECKING_ID, checkingDepositAmount);
        bank.deposit(SAVINGS_ID, savingsDepositAmount);

        assertTrue(passTimeProcessor.handle(String.format("%s %s", transactionType, months)));
        assertEquals(passTime(minBalanceFee, months, AccountType.Checking, apr, checkingDepositAmount), bank.getAccount(CHECKING_ID).getBalance());
        assertEquals(passTime(minBalanceFee, months, AccountType.Savings, apr, savingsDepositAmount), bank.getAccount(SAVINGS_ID).getBalance());
        assertEquals(passTime(minBalanceFee, months, AccountType.CD, apr, initialCDBalance), bank.getAccount(CD_ID).getBalance());
    }

    @Test
    protected void transaction_when_balance_is_less_than_or_equal_to_100_should_apply_min_balance_fee_then_apr() {
        double minBalanceFee = bank.getMinBalanceFee();
        int months = 2;
        TransactionType transactionType = TransactionType.PassTime;
        double checkingDepositAmount = 75;
        double savingsDepositAmount = 100;

        bank.deposit(CHECKING_ID, checkingDepositAmount);
        bank.deposit(SAVINGS_ID, savingsDepositAmount);

        assertTrue(passTimeProcessor.handle(String.format("%s %s", transactionType, months)));
        assertEquals(passTime(minBalanceFee, months, AccountType.Checking, apr, checkingDepositAmount), bank.getAccount(CHECKING_ID).getBalance());
        assertEquals(passTime(minBalanceFee, months, AccountType.Savings, apr, savingsDepositAmount), bank.getAccount(SAVINGS_ID).getBalance());
        assertEquals(passTime(minBalanceFee, months, AccountType.CD, apr, initialCDBalance), bank.getAccount(CD_ID).getBalance());
    }

    @Test
    protected void transaction_when_balance_is_0_should_remove_account() {
        double minBalanceFee = bank.getMinBalanceFee();
        int months = 2;
        TransactionType transactionType = TransactionType.PassTime;
        double depositAmount = 25;

        bank.deposit(SAVINGS_ID, depositAmount);

        assertTrue(passTimeProcessor.handle(String.format("%s %s", transactionType, months)));
        assertFalse(bank.containsAccount(CHECKING_ID));
        assertFalse(bank.containsAccount(SAVINGS_ID));
        assertTrue(bank.containsAccount(CD_ID));
        assertEquals(passTime(minBalanceFee, months, AccountType.CD, apr, initialCDBalance), bank.getAccount(CD_ID).getBalance());
    }

    @Test
    protected void transaction_should_be_case_insensitive() {
        int months = getMonthsPerYear();

        assertTrue(passTimeProcessor.handle(String.format("%s %s", "PaSs tIme", months)));
    }

    @Test
    protected void transaction_should_be_possible_with_useless_additional_arguments() {
        int months = getMonthsPerYear();
        TransactionType transactionType = TransactionType.PassTime;

        assertTrue(passTimeProcessor.handle(String.format("%s %s %s", transactionType, months, "0")));
        assertTrue(passTimeProcessor.handle(String.format("%s %s %s %s %s %s", transactionType, months, 89, 23892398, 92839233, 23)));
    }
}
