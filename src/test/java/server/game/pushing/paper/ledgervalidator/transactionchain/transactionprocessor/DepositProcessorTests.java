package server.game.pushing.paper.ledgervalidator.transactionchain.transactionprocessor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.game.pushing.paper.ledgervalidator.bank.Bank;
import server.game.pushing.paper.ledgervalidator.bank.account.Checking;
import server.game.pushing.paper.ledgervalidator.bank.account.Savings;
import server.game.pushing.paper.ledgervalidator.transactionchain.TransactionType;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static server.game.pushing.paper.ledgervalidator.bank.Bank.getMaxAPR;

public class DepositProcessorTests {
    protected Bank bank;
    protected DepositProcessor depositProcessor;

    protected final String CHECKING_ID = "87439742";
    protected final String SAVINGS_ID = "97520943";
    protected final double APR = getMaxAPR();

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
        String id = SAVINGS_ID;
        double depositAmount = bank.getAccount(id).getMaxDepositAmount();
        double withdrawAmount = 1000;

        bank.deposit(id, depositAmount);
        depositProcessor.setNext(new WithdrawProcessor(bank));

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
}
