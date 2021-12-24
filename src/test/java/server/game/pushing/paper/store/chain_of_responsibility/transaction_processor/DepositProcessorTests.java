package server.game.pushing.paper.store.chain_of_responsibility.transaction_processor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.game.pushing.paper.store.bank.Bank;
import server.game.pushing.paper.store.chain_of_responsibility.ChainOfResponsibility;
import server.game.pushing.paper.store.chain_of_responsibility.TransactionType;

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

        bank.createCheckingAccount(CHECKING_ID);
        bank.createSavingsAccount(SAVINGS_ID);
    }

    @Test
    protected void deposit_processors_can_handle_deposit_transactions_to_checking() {
        String id = CHECKING_ID;
        double depositAmount = bank.getAccount(id).getMaxDepositAmount();

        assertTrue(processor.handle(String.format("%s %s %s", transactionType, id, depositAmount)));
        assertEquals(depositAmount, bank.getAccount(id).getBalance());
    }

    @Test
    protected void deposit_processors_can_handle_deposit_transactions_to_savings() {
        String id = SAVINGS_ID;
        double depositAmount = bank.getAccount(id).getMaxDepositAmount();

        assertTrue(processor.handle(String.format("%s %s %s", transactionType, id, depositAmount)));
        assertEquals(depositAmount, bank.getAccount(id).getBalance());
    }

    @Test
    protected void deposit_processors_can_ignore_additional_arguments() {
        assertTrue(processor.handle(String.format("%s %s %s %s", transactionType, CHECKING_ID, bank.getAccount(CHECKING_ID).getMaxDepositAmount(), "nuke")));
        assertTrue(processor.handle(String.format("%s %s %s %s %s %s  %s %s  %s %s", transactionType, SAVINGS_ID, bank.getAccount(SAVINGS_ID).getMaxDepositAmount(), "0", "0", "0", "0", "0", "0", "0")));
    }

    @Test
    protected void deposit_processors_are_case_insensitive() {
        assertTrue(processor.handle(String.format("dePoSIT %s %s", CHECKING_ID, bank.getAccount(CHECKING_ID).getMaxDepositAmount())));
        assertTrue(processor.handle(String.format("deposit %s %s", SAVINGS_ID, bank.getAccount(SAVINGS_ID).getMaxDepositAmount())));
    }

    @Test
    protected void deposit_processors_can_be_in_a_chain_of_responsibility() {
        String payingID = SAVINGS_ID;
        String receivingID = CHECKING_ID;
        double depositAmount = bank.getAccount(payingID).getMaxDepositAmount();
        double withdrawAmount = bank.getAccount(payingID).getMaxWithdrawAmount();
        double transferAmount = min(bank.getAccount(payingID).getMaxWithdrawAmount(), bank.getAccount(receivingID).getMaxDepositAmount());
        assertTrue(processor.handle(String.format("%s %s %s", transactionType, payingID, depositAmount)));

        processor.setNext(new WithdrawProcessor(bank));

        assertTrue(processor.handle(String.format("%s %s %s", TransactionType.Withdraw, payingID, withdrawAmount)));
        assertEquals(depositAmount - withdrawAmount, bank.getAccount(payingID).getBalance());
        assertFalse(processor.handle(String.format("%s %s %s %s", TransactionType.Transfer, payingID, receivingID, transferAmount)));
    }
}
