package server.game.pushing.paper.processor.transaction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.game.pushing.paper.bank.Bank;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

public class DepositProcessorTests {
    protected DepositProcessor depositProcessor;
    protected Bank bank;
    protected final String CHECKING_ID = "00000000";
    protected final String SAVINGS_ID = "00000001";
    protected final double APR = 0.1;

    @BeforeEach
    void setUp() {
        bank = new Bank();
        depositProcessor = new DepositProcessor(bank);

        depositProcessor.setNextHandler(new CreateProcessor(bank));
        depositProcessor.handle(String.format("create checking %s %f", CHECKING_ID, APR));
        depositProcessor.handle(String.format("create savings %s %f", SAVINGS_ID, APR));
        depositProcessor.setNextHandler(null);
    }

    @Test
    protected void deposit_processor_when_transaction_can_not_process_should_pass_transaction_up_the_chain_of_responsibility() {
        double savingsDepositAmount = 2500;
        double savingsWithdrawAmount = 1000;

        depositProcessor.setNextHandler(new WithdrawProcessor(bank));
        depositProcessor.handle(String.format("deposit %s %f", SAVINGS_ID, savingsDepositAmount));
        depositProcessor.handle(String.format("withdraw %s %f", SAVINGS_ID, savingsWithdrawAmount));

        assertEquals(savingsDepositAmount - savingsWithdrawAmount, bank.getAccount(SAVINGS_ID).getBalance());
        assertFalse(depositProcessor.handle(String.format("transfer %s %s %f", SAVINGS_ID, CHECKING_ID, 400.0f)));
    }

    @Test
    void deposit_checking_transaction_should_be_process() {
        double checkingDepositAmount = 200;

        depositProcessor.handle(String.format("deposit %s %f", CHECKING_ID, checkingDepositAmount));

        assertEquals(checkingDepositAmount, bank.getAccount(CHECKING_ID).getBalance());
    }

    @Test
    void deposit_savings_transaction_should_be_process() {
        double savingsDepositAmount = 300;

        depositProcessor.handle(String.format("deposit %s %f", SAVINGS_ID, savingsDepositAmount));

        assertEquals(savingsDepositAmount, bank.getAccount(SAVINGS_ID).getBalance());
    }
}
