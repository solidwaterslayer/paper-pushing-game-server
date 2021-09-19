package server.game.pushing.paper.store.chainofresponsibility.transactionprocessor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.game.pushing.paper.store.bank.Bank;
import server.game.pushing.paper.store.chainofresponsibility.ChainOfResponsibility;
import server.game.pushing.paper.store.chainofresponsibility.TransactionType;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

public class DepositProcessorTests {
    private Bank bank;
    private DepositProcessor depositProcessor;

    private final String CHECKING_ID = "87439742";
    private final String SAVINGS_ID = "97520943";

    @BeforeEach
    protected void setUp() {
        bank = new Bank();
        depositProcessor = new DepositProcessor(bank);

        double apr = bank.getMaxAPR();

        bank.createChecking(CHECKING_ID, apr);
        bank.createSavings(SAVINGS_ID, apr);
    }

    @Test
    protected void deposit_processor_when_transaction_can_not_process_should_pass_transaction_up_the_chain_of_responsibility() {
        depositProcessor = (DepositProcessor) ChainOfResponsibility.getInstance(Arrays.asList(depositProcessor, new WithdrawProcessor(bank), null));

        String id = SAVINGS_ID;
        double depositAmount = bank.getAccount(id).getMaxDepositAmount();
        double withdrawAmount = 1000;

        bank.deposit(id, depositAmount);

        assertTrue(depositProcessor.handle(String.format("%s %s %s", TransactionType.Withdraw, id, withdrawAmount)));
        assertEquals(depositAmount - withdrawAmount, bank.getAccount(id).getBalance());
        assertFalse(depositProcessor.handle(String.format("%s %s %s %s", TransactionType.Transfer, id, CHECKING_ID, withdrawAmount)));
    }

    @Test
    protected void deposit_checking_transaction_should_be_process() {
        TransactionType transactionType = TransactionType.Deposit;
        String id = CHECKING_ID;
        double depositAmount = bank.getAccount(id).getMaxDepositAmount();

        assertTrue(depositProcessor.handle(String.format("%s %s %s", transactionType, id, depositAmount)));
        assertEquals(depositAmount, bank.getAccount(id).getBalance());
    }

    @Test
    protected void deposit_savings_transaction_should_be_process() {
        TransactionType transactionType = TransactionType.Deposit;
        String id = SAVINGS_ID;
        double depositAmount = bank.getAccount(id).getMaxDepositAmount();

        assertTrue(depositProcessor.handle(String.format("%s %s %s", transactionType, id, depositAmount)));
        assertEquals(depositAmount, bank.getAccount(id).getBalance());
    }

    @Test
    protected void transaction_should_be_case_insensitive() {
        assertTrue(depositProcessor.handle(String.format("dePoSIT %s %s", CHECKING_ID, bank.getAccount(CHECKING_ID).getMaxDepositAmount())));
        assertTrue(depositProcessor.handle(String.format("deposit %s %s", SAVINGS_ID, bank.getAccount(SAVINGS_ID).getMaxDepositAmount())));
    }

    @Test
    protected void transaction_should_be_possible_with_useless_additional_arguments() {
        TransactionType transactionType = TransactionType.Deposit;

        assertTrue(depositProcessor.handle(String.format("%s %s %s %s", transactionType, CHECKING_ID, bank.getAccount(CHECKING_ID).getMaxDepositAmount(), "nuke")));
        assertTrue(depositProcessor.handle(String.format("%s %s %s %s %s %s  %s %s  %s %s", transactionType, SAVINGS_ID, bank.getAccount(SAVINGS_ID).getMaxDepositAmount(), "0", "0", "0", "0", "0", "0", "0")));
    }
}
