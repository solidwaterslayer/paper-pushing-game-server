package server.game.pushing.paper.processor.transaction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.game.pushing.paper.bank.Bank;
import server.game.pushing.paper.bank.account.AccountType;
import server.game.pushing.paper.validator.transaction.CreateValidator;
import server.game.pushing.paper.validator.transaction.DepositValidator;

import static org.junit.jupiter.api.Assertions.*;

public class CreateProcessorTests {
    CreateProcessor createProcessor;
    protected Bank bank;

    protected final String CHECKING_ID = "00000000";
    protected final String SAVINGS_ID = "00000001";
    protected final String CD_ID = "00000010";
    protected final double APR = 10;
    protected final double INITIAL_CD_BALANCE = 8000;

    @BeforeEach
    protected void setUp() {
        bank = new Bank();
        createProcessor = new CreateProcessor(bank);
    }

    @Test
    protected void create_processor_when_transaction_can_not_process_should_pass_transaction_up_the_chain_of_responsibility() {
        double savingsDepositAmount = 2500;

        createProcessor.setNextHandler(new DepositProcessor(bank));
        createProcessor.handle(String.format("create savings %s %f", SAVINGS_ID, APR));
        assertTrue(createProcessor.handle(String.format("deposit %s %f", SAVINGS_ID, savingsDepositAmount)));

        assertEquals(savingsDepositAmount, bank.getAccount(SAVINGS_ID).getBalance());
        assertFalse(createProcessor.handle(String.format("withdraw %s %f", SAVINGS_ID, 1000.0f)));
    }

    @Test
    protected void create_checking_transaction_should_process() {
        createProcessor.handle(String.format("create checking %s %f", CHECKING_ID, APR));

        assertEquals(AccountType.Checking, bank.getAccount(CHECKING_ID).getAccountType());
        assertEquals(CHECKING_ID, bank.getAccount(CHECKING_ID).getID());
        assertEquals(APR, bank.getAccount(CHECKING_ID).getAPR());
        assertEquals(0, bank.getAccount(CHECKING_ID).getBalance());
    }

    @Test
    protected void create_savings_transaction_should_process() {
        createProcessor.handle(String.format("create savings %s %f", SAVINGS_ID, APR));

        assertEquals(AccountType.Savings, bank.getAccount(SAVINGS_ID).getAccountType());
        assertEquals(SAVINGS_ID, bank.getAccount(SAVINGS_ID).getID());
        assertEquals(APR, bank.getAccount(SAVINGS_ID).getAPR());
        assertEquals(0, bank.getAccount(SAVINGS_ID).getBalance());
    }

    @Test
    protected void create_cd_transaction_should_process() {
        createProcessor.handle(String.format("create cd %s %f %f", CD_ID, APR, INITIAL_CD_BALANCE));

        assertEquals(AccountType.CD, bank.getAccount(CD_ID).getAccountType());
        assertEquals(CD_ID, bank.getAccount(CD_ID).getID());
        assertEquals(APR, bank.getAccount(CD_ID).getAPR());
        assertEquals(INITIAL_CD_BALANCE, bank.getAccount(CD_ID).getBalance());
    }
}
