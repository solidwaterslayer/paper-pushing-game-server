package server.game.pushing.paper.store.chain_of_responsibility.transaction_processor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.game.pushing.paper.store.bank.Bank;
import server.game.pushing.paper.store.chain_of_responsibility.ChainOfResponsibility;
import server.game.pushing.paper.store.chain_of_responsibility.TransactionType;

import java.util.Arrays;

import static java.lang.Math.min;
import static org.junit.jupiter.api.Assertions.*;

public class DepositProcessorTests {
    private Bank bank;
    private ChainOfResponsibility processor;

    private TransactionType transactionType;
    private final String CHECKING_ID = "87439742";
    private final String SAVINGS_ID = "97520943";

    @BeforeEach
    protected void setUp() {
        bank = new Bank();
        processor = new DepositProcessor(bank);

        transactionType = processor.getTransactionType();
        double apr = bank.getMaxAPR();

        bank.createChecking(CHECKING_ID, apr);
        bank.createSavings(SAVINGS_ID, apr);
    }

    @Test
    protected void deposit_processor_when_transaction_can_not_process_should_pass_transaction_up_the_chain_of_responsibility() {
        processor = ChainOfResponsibility.getInstance(Arrays.asList(processor, new WithdrawProcessor(bank), null));

        String fromID = SAVINGS_ID;
        String toID = CHECKING_ID;
        double depositAmount = bank.getAccount(fromID).getMaxDepositAmount();
        double withdrawAmount = bank.getAccount(fromID).getMaxWithdrawAmount();
        double transferAmount = min(bank.getAccount(fromID).getMaxWithdrawAmount(), bank.getAccount(toID).getMaxDepositAmount());
        assertTrue(processor.handle(String.format("%s %s %s", transactionType, fromID, depositAmount)));

        assertTrue(processor.handle(String.format("%s %s %s", TransactionType.Withdraw, fromID, withdrawAmount)));
        assertEquals(depositAmount - withdrawAmount, bank.getAccount(fromID).getBalance());
        assertFalse(processor.handle(String.format("%s %s %s %s", TransactionType.Transfer, fromID, toID, transferAmount)));
    }

    @Test
    protected void deposit_checking_transaction_should_be_process() {
        String id = CHECKING_ID;
        double depositAmount = bank.getAccount(id).getMaxDepositAmount();

        assertTrue(processor.handle(String.format("%s %s %s", transactionType, id, depositAmount)));
        assertEquals(depositAmount, bank.getAccount(id).getBalance());
    }

    @Test
    protected void deposit_savings_transaction_should_be_process() {
        String id = SAVINGS_ID;
        double depositAmount = bank.getAccount(id).getMaxDepositAmount();

        assertTrue(processor.handle(String.format("%s %s %s", transactionType, id, depositAmount)));
        assertEquals(depositAmount, bank.getAccount(id).getBalance());
    }

    @Test
    protected void transaction_should_be_case_insensitive() {
        assertTrue(processor.handle(String.format("dePoSIT %s %s", CHECKING_ID, bank.getAccount(CHECKING_ID).getMaxDepositAmount())));
        assertTrue(processor.handle(String.format("deposit %s %s", SAVINGS_ID, bank.getAccount(SAVINGS_ID).getMaxDepositAmount())));
    }

    @Test
    protected void transaction_should_be_possible_with_useless_additional_arguments() {
        assertTrue(processor.handle(String.format("%s %s %s %s", transactionType, CHECKING_ID, bank.getAccount(CHECKING_ID).getMaxDepositAmount(), "nuke")));
        assertTrue(processor.handle(String.format("%s %s %s %s %s %s  %s %s  %s %s", transactionType, SAVINGS_ID, bank.getAccount(SAVINGS_ID).getMaxDepositAmount(), "0", "0", "0", "0", "0", "0", "0")));
    }
}
