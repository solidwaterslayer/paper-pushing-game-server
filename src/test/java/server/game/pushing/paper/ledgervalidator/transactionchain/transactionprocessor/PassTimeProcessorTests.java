package server.game.pushing.paper.ledgervalidator.transactionchain.transactionprocessor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.game.pushing.paper.ledgervalidator.bank.Bank;
import server.game.pushing.paper.ledgervalidator.bank.BankTests;
import server.game.pushing.paper.ledgervalidator.bank.account.AccountType;
import server.game.pushing.paper.ledgervalidator.bank.account.CD;
import server.game.pushing.paper.ledgervalidator.bank.account.Checking;
import server.game.pushing.paper.ledgervalidator.bank.account.Savings;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static server.game.pushing.paper.ledgervalidator.bank.Bank.getMaxAPR;
import static server.game.pushing.paper.ledgervalidator.bank.Bank.getMinInitialCDBalance;

public class PassTimeProcessorTests {
    protected Bank bank;
    protected PassTimeProcessor passTimeProcessor;

    protected final String CHECKING_ID = "98430842";
    protected final String SAVINGS_ID = "89438042";
    protected final String CD_ID = "98430842";
    protected final double APR = getMaxAPR();
    protected final double INITIAL_CD_BALANCE = getMinInitialCDBalance();

    @BeforeEach
    void setUp() {
        bank = new Bank(Arrays.asList(
                new Checking(CHECKING_ID, APR),
                new Savings(SAVINGS_ID, APR),
                new CD(CD_ID, APR, INITIAL_CD_BALANCE)
        ));
        passTimeProcessor = new PassTimeProcessor(bank);
    }

    @Test
    protected void pass_time_processor_when_transaction_can_not_process_should_pass_transaction_up_the_chain_of_responsibility() {
        String id = "10000010";

        passTimeProcessor.setNext(new CreateProcessor(bank));
        assertTrue(passTimeProcessor.handle(String.format("create cd %s %f %f", id, APR, INITIAL_CD_BALANCE)));

        assertEquals(AccountType.CD, bank.getAccount(id).getAccountType());
        assertEquals(id, bank.getAccount(id).getID());
        assertEquals(APR, bank.getAccount(id).getAPR());
        assertEquals(INITIAL_CD_BALANCE, bank.getAccount(id).getBalance());
        assertFalse(passTimeProcessor.handle(String.format("deposit %s %f", SAVINGS_ID, 2000.0f)));
    }

    @Test
    protected void pass_time_transaction_should_apply_apr() {
        double minBalanceFee = bank.getMinBalanceFee();
        int months = 6;
        double checkingDepositAmount = 1000;
        double savingsDepositAmount = 2500;

        bank.deposit(CHECKING_ID, checkingDepositAmount);
        bank.deposit(SAVINGS_ID, savingsDepositAmount);
        passTimeProcessor.handle(String.format("pass time %d", months));

        assertEquals(BankTests.passTime(APR, minBalanceFee, AccountType.Checking, checkingDepositAmount, months), bank.getAccount(CHECKING_ID).getBalance());
        assertEquals(BankTests.passTime(APR, minBalanceFee, AccountType.Savings, savingsDepositAmount, months), bank.getAccount(SAVINGS_ID).getBalance());
        assertEquals(BankTests.passTime(APR, minBalanceFee, AccountType.CD, INITIAL_CD_BALANCE, months), bank.getAccount(CD_ID).getBalance());
    }

    @Test
    protected void pass_time_transaction_when_balance_is_less_than_or_equal_to_100_should_apply_min_balance_fee_then_apr() {
        double minBalanceFee = bank.getMinBalanceFee();
        int months = 2;
        double checkingDepositAmount = 75;
        double savingsDepositAmount = 100;

        bank.deposit(CHECKING_ID, checkingDepositAmount);
        bank.deposit(SAVINGS_ID, savingsDepositAmount);
        bank.passTime(months);

        assertEquals(BankTests.passTime(APR, minBalanceFee, AccountType.Checking, checkingDepositAmount, months), bank.getAccount(CHECKING_ID).getBalance());
        assertEquals(BankTests.passTime(APR, minBalanceFee, AccountType.Savings, savingsDepositAmount, months), bank.getAccount(SAVINGS_ID).getBalance());
        assertEquals(BankTests.passTime(APR, minBalanceFee, AccountType.CD, INITIAL_CD_BALANCE, months), bank.getAccount(CD_ID).getBalance());
    }

    @Test
    protected void pass_time_transaction_when_balance_is_0_should_remove_account() {
        double minBalanceFee = bank.getMinBalanceFee();
        int months = 2;

        bank.deposit(SAVINGS_ID, 25);
        bank.passTime(months);

        assertFalse(bank.containsAccount(CHECKING_ID));
        assertFalse(bank.containsAccount(SAVINGS_ID));
        assertTrue(bank.containsAccount(CD_ID));
        assertEquals(BankTests.passTime(APR, minBalanceFee, AccountType.CD, INITIAL_CD_BALANCE, months), bank.getAccount(CD_ID).getBalance());
    }
}
