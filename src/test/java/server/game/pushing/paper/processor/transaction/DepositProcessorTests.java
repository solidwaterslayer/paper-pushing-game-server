package server.game.pushing.paper.processor.transaction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.game.pushing.paper.bank.Bank;
import server.game.pushing.paper.bank.account.Checking;
import server.game.pushing.paper.bank.account.Savings;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class DepositProcessorTests {
    protected DepositProcessor depositProcessor;
    protected Bank bank;
    protected final String CHECKING_ID = "00000000";
    protected final String SAVINGS_ID = "00000001";
    protected final double APR = 9;

    @BeforeEach
    protected void setUp() {
        bank = new Bank(Arrays.asList(
                new Checking(CHECKING_ID, APR),
                new Savings(SAVINGS_ID, APR)
        ));
        depositProcessor = new DepositProcessor(bank);
    }

    @Test
    protected void deposit_processor_when_transaction_can_not_process_should_pass_transaction_up_the_chain_of_responsibility() {
        double savingsDepositAmount = 2500;
        double savingsWithdrawAmount = 1000;

        depositProcessor.setNext(new WithdrawProcessor(bank));
        depositProcessor.handle(String.format("deposit %s %f", SAVINGS_ID, savingsDepositAmount));
        assertTrue(depositProcessor.handle(String.format("withdraw %s %f", SAVINGS_ID, savingsWithdrawAmount)));

        assertEquals(savingsDepositAmount - savingsWithdrawAmount, bank.getAccount(SAVINGS_ID).getBalance());
        assertFalse(depositProcessor.handle(String.format("transfer %s %s %f", SAVINGS_ID, CHECKING_ID, 400.0f)));
    }

    @Test
    protected void deposit_checking_transaction_should_be_process() {
        double checkingDepositAmount = 200;

        depositProcessor.handle(String.format("deposit %s %f", CHECKING_ID, checkingDepositAmount));

        assertEquals(checkingDepositAmount, bank.getAccount(CHECKING_ID).getBalance());
    }

    @Test
    protected void deposit_savings_transaction_should_be_process() {
        double savingsDepositAmount = 300;

        depositProcessor.handle(String.format("deposit %s %f", SAVINGS_ID, savingsDepositAmount));

        assertEquals(savingsDepositAmount, bank.getAccount(SAVINGS_ID).getBalance());
    }
}
