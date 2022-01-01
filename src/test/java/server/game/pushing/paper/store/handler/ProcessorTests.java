package server.game.pushing.paper.store.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.game.pushing.paper.TransactionType;
import server.game.pushing.paper.store.bank.Bank;
import server.game.pushing.paper.store.handler.processor.*;

import static java.lang.Math.min;
import static org.junit.jupiter.api.Assertions.*;
import static server.game.pushing.paper.store.BankTests.timeTravel;
import static server.game.pushing.paper.store.bank.AccountType.*;

public class ProcessorTests {
    private Bank bank;
    private Handler createProcessor;
    private Handler timeTravelProcessor;
    private Handler depositProcessor;
    private Handler withdrawProcessor;
    private Handler transferProcessor;

    private final String payingID = "12341234";
    private final String receivingID = "87549753";

    @BeforeEach
    protected void setUp() {
        bank = new Bank();

        createProcessor = new CreateProcessor(bank);
        timeTravelProcessor = new TimeTravelProcessor(bank);
        depositProcessor = new DepositProcessor(bank);
        withdrawProcessor = new WithdrawProcessor(bank);
        transferProcessor = new TransferProcessor(bank);

        bank.createCheckingAccount(payingID);
        bank.createSavingsAccount(receivingID);
    }

    @Test
    protected void a_create_transaction_create_accounts() {
        TransactionType transactionType = createProcessor.getTransactionType();
        String checkingID = "98439811";
        String savingsID = "11112222";
        String cdID = "12345432";
        double cdBalance = bank.getMinCDBalance();

        assertTrue(createProcessor.handleTransaction(String.format("%s %s %s", transactionType, Checking, checkingID)));
        assertTrue(createProcessor.handleTransaction(String.format("%s %s %s", transactionType, Savings, savingsID)));
        assertTrue(createProcessor.handleTransaction(String.format("%s %s %s %s", transactionType, CD, cdID, cdBalance)));
        assertNotNull(bank.getAccount(checkingID));
        assertNotNull(bank.getAccount(savingsID));
        assertNotNull(bank.getAccount(cdID));
    }

    @Test
    protected void a_time_travel_transaction_can_time_travel() {
        TransactionType transactionType = timeTravelProcessor.getTransactionType();
        String id = receivingID;
        int months = 8;
        double depositAmount = 900;

        bank.deposit(id, depositAmount);

        assertTrue(timeTravelProcessor.handleTransaction(String.format("%s %s", transactionType, months)));
        assertEquals(timeTravel(depositAmount, months), bank.getAccount(id).getBalance());
    }

    @Test
    protected void a_deposit_transaction_can_deposit_to_accounts() {
        TransactionType transactionType = depositProcessor.getTransactionType();
        String id = payingID;
        double depositAmount = bank.getAccount(id).getMaxDepositAmount();

        assertTrue(depositProcessor.handleTransaction(String.format("%s %s %s", transactionType, id, depositAmount)));
        assertEquals(depositAmount, bank.getAccount(id).getBalance());
    }

    @Test
    protected void a_withdraw_transaction_can_withdraw_from_accounts() {
        TransactionType transactionType = withdrawProcessor.getTransactionType();
        String id = receivingID;
        double depositAmount = bank.getAccount(id).getMaxDepositAmount();
        double withdrawAmount = bank.getAccount(id).getMaxWithdrawAmount();

        bank.deposit(id, depositAmount);

        assertTrue(withdrawProcessor.handleTransaction(String.format("%s %s %s", transactionType, id, withdrawAmount)));
        assertEquals(depositAmount - withdrawAmount, bank.getAccount(id).getBalance());
    }

    @Test
    protected void a_transfer_transaction_can_transfer_between_accounts() {
        TransactionType transactionType = transferProcessor.getTransactionType();
        String payingID = this.payingID;
        String receivingID = this.receivingID;
        double depositAmount = bank.getAccount(payingID).getMaxDepositAmount();
        double transferAmount = min(bank.getAccount(payingID).getMaxWithdrawAmount(), bank.getAccount(receivingID).getMaxDepositAmount());

        bank.deposit(payingID, depositAmount);

        assertTrue(transferProcessor.handleTransaction(String.format("%s %s %s %s", transactionType, payingID, receivingID, transferAmount)));
        assertEquals(depositAmount - transferAmount, bank.getAccount(payingID).getBalance());
        assertEquals(transferAmount, bank.getAccount(receivingID).getBalance());
    }
}
