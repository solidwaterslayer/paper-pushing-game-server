package server.game.pushing.paper.store.chain_of_responsibility.transaction_processor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.game.pushing.paper.store.bank.Bank;
import server.game.pushing.paper.store.bank.account.Account;
import server.game.pushing.paper.store.bank.account.AccountType;
import server.game.pushing.paper.store.chain_of_responsibility.ChainOfResponsibility;
import server.game.pushing.paper.store.chain_of_responsibility.TransactionType;

import static org.junit.jupiter.api.Assertions.*;
import static server.game.pushing.paper.store.bank.Bank.getMonthsPerYear;
import static server.game.pushing.paper.store.bank.BankTests.timeTravel;

public class TimeTravelProcessorTests {
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
        processor = new TimeTravelProcessor(bank);

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
    protected void pass_time_processor_when_transaction_can_not_process_should_pass_transaction_down_the_chain_of_responsibility() {
        String id0 = "10000010";
        String id1 = SAVINGS_ID;

        processor.setNext(new CreateProcessor(bank));

        assertTrue(processor.handle(String.format("%s %s %s %s %s", TransactionType.Create, AccountType.CD, id0, apr, initialCDBalance)));
        Account account = bank.getAccount(id0);
        assertEquals(AccountType.CD, account.getAccountType());
        assertEquals(id0, account.getID());
        assertEquals(apr, account.getAPR());
        assertEquals(initialCDBalance, account.getBalance());
        assertFalse(processor.handle(String.format("%s %s %s", TransactionType.Deposit, id1, bank.getAccount(id1).getMaxDepositAmount())));
    }

    @Test
    protected void transaction_when_balance_is_less_than_or_equal_to_100_should_apply_min_balance_fee() {
        months = 2;
        double checkingDepositAmount = 75;
        double savingsDepositAmount = 100;

        bank.deposit(CHECKING_ID, checkingDepositAmount);
        bank.deposit(SAVINGS_ID, savingsDepositAmount);

        assertTrue(processor.handle(String.format("%s %s", transactionType, months)));
        assertEquals(timeTravel(minBalanceFee, months, checkingDepositAmount), bank.getAccount(CHECKING_ID).getBalance());
        assertEquals(timeTravel(minBalanceFee, months, savingsDepositAmount), bank.getAccount(SAVINGS_ID).getBalance());
        assertEquals(timeTravel(minBalanceFee, months, initialCDBalance), bank.getAccount(CD_ID).getBalance());
    }

    @Test
    protected void transaction_should_be_case_insensitive() {
        assertTrue(processor.handle(String.format("%s %s", "tImE tRaVel", months)));
    }

    @Test
    protected void transaction_should_be_possible_with_useless_additional_arguments() {
        assertTrue(processor.handle(String.format("%s %s %s", transactionType, months, "0")));
        assertTrue(processor.handle(String.format("%s %s %s %s %s %s", transactionType, months, 89, 23892398, 92839233, 23)));
    }
}
