package server.game.pushing.paper.store.chain_of_responsibility.transaction_processor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.game.pushing.paper.store.bank.Bank;
import server.game.pushing.paper.store.bank.AccountType;
import server.game.pushing.paper.store.chain_of_responsibility.ChainOfResponsibility;
import server.game.pushing.paper.store.chain_of_responsibility.TransactionType;

import static java.lang.Math.min;
import static org.junit.jupiter.api.Assertions.*;
import static server.game.pushing.paper.store.bank.Bank.getMonthsPerYear;
import static server.game.pushing.paper.store.BankTests.timeTravel;

public class TransferProcessorTests {
    private Bank bank;
    private ChainOfResponsibility processor;

    private final int MONTHS = getMonthsPerYear();
    private TransactionType transactionType;
    private final String CHECKING_ID_0 = "98830842";
    private final String CHECKING_ID_1 = "09309843";
    private final String SAVINGS_ID_0 = "90328934";
    private final String SAVINGS_ID_1 = "11117823";
    private final String CD_ID = "08429834";
    private double cdBalance;
    private double checkingDepositAmount;
    private double savingsDepositAmount;

    @BeforeEach
    protected void setUp() {
        bank = new Bank();
        processor = new TransferProcessor(bank);

        transactionType = processor.getTransactionType();
        cdBalance = bank.getMinCDBalance();
        bank.createCheckingAccount(CHECKING_ID_0);
        bank.createCheckingAccount(CHECKING_ID_1);
        bank.createSavingsAccount(SAVINGS_ID_0);
        bank.createSavingsAccount(SAVINGS_ID_1);
        bank.createCDAccount(CD_ID, cdBalance);
        checkingDepositAmount = bank.getAccount(CHECKING_ID_1).getMaxDepositAmount();
        savingsDepositAmount = bank.getAccount(SAVINGS_ID_1).getMaxDepositAmount();

        bank.deposit(CHECKING_ID_0, checkingDepositAmount);
        bank.deposit(CHECKING_ID_1, checkingDepositAmount);
        bank.deposit(SAVINGS_ID_0, savingsDepositAmount);
        bank.deposit(SAVINGS_ID_1, savingsDepositAmount);
    }

    @Test
    protected void transfer_processors_can_handle_transfer_transactions_from_checking_to_checking() {
        String payingID = CHECKING_ID_0;
        String receivingID = CHECKING_ID_1;
        double transferAmount = min(bank.getAccount(payingID).getMaxWithdrawAmount(), bank.getAccount(receivingID).getMaxDepositAmount());

        assertTrue(processor.handle(String.format("%s %s %s %s", transactionType, payingID, receivingID, transferAmount)));
        assertEquals(checkingDepositAmount - transferAmount, bank.getAccount(payingID).getBalance());
        assertEquals(checkingDepositAmount + transferAmount, bank.getAccount(receivingID).getBalance());
    }

    @Test
    protected void transfer_processors_can_handle_transfer_transactions_from_checking_to_savings_transaction() {
        String payingID = CHECKING_ID_0;
        String receivingID = SAVINGS_ID_0;
        double transferAmount = min(bank.getAccount(payingID).getMaxWithdrawAmount(), bank.getAccount(receivingID).getMaxDepositAmount());

        assertTrue(processor.handle(String.format("%s %s %s %s", transactionType, payingID, receivingID, transferAmount)));
        assertEquals(checkingDepositAmount - transferAmount, bank.getAccount(payingID).getBalance());
        assertEquals(savingsDepositAmount + transferAmount, bank.getAccount(receivingID).getBalance());
    }

    @Test
    protected void transfer_processors_can_handle_transfer_transactions_from_savings_to_checking_transaction() {
        String payingID = SAVINGS_ID_1;
        String receivingID = CHECKING_ID_0;
        double transferAmount = min(bank.getAccount(payingID).getMaxWithdrawAmount(), bank.getAccount(receivingID).getMaxDepositAmount());

        assertTrue(processor.handle(String.format("%s %s %s %s", transactionType, payingID, receivingID, transferAmount)));
        assertEquals(savingsDepositAmount - transferAmount, bank.getAccount(payingID).getBalance());
        assertEquals(checkingDepositAmount + transferAmount, bank.getAccount(receivingID).getBalance());
    }

    @Test
    protected void transfer_processors_can_handle_transfer_transactions_from_savings_to_savings_transaction() {
        String payingID = SAVINGS_ID_1;
        String receivingID = SAVINGS_ID_0;
        double transferAmount = min(bank.getAccount(payingID).getMaxWithdrawAmount(), bank.getAccount(receivingID).getMaxDepositAmount());

        assertTrue(processor.handle(String.format("%s %s %s %s", transactionType, payingID, receivingID, transferAmount)));
        assertEquals(savingsDepositAmount - transferAmount, bank.getAccount(payingID).getBalance());
        assertEquals(savingsDepositAmount + transferAmount, bank.getAccount(receivingID).getBalance());
    }

    @Test
    protected void transfer_processors_can_handle_transfer_transactions_from_cd_to_savings_transaction() {
        String payingID = CD_ID;
        String receivingID = SAVINGS_ID_1;
        double transferAmount = min(bank.getAccount(payingID).getMaxWithdrawAmount(), bank.getAccount(receivingID).getMaxDepositAmount());

        bank.timeTravel(MONTHS);

        assertTrue(processor.handle(String.format("%s %s %s %s", transactionType, payingID, receivingID, transferAmount)));
        assertEquals(0, bank.getAccount(payingID).getBalance());
        assertEquals(
                timeTravel(savingsDepositAmount, MONTHS)
                        + timeTravel(cdBalance, MONTHS)
                , bank.getAccount(receivingID).getBalance()
        );
    }

    @Test
    protected void transfer_processors_can_transfer_when_the_transfer_amount_is_less_than_or_equal_to_the_paying_account_balance() {
        String id0 = SAVINGS_ID_0;
        String id1 = CHECKING_ID_1;
        double transferAmount = 400;

        for (int i = 0; i < 3; i++) {
            bank.withdraw(SAVINGS_ID_0, 700);
        }

        assertEquals(transferAmount, bank.getAccount(id0).getBalance());
        assertTrue(processor.handle(String.format("%s %s %s %s", transactionType, id0, id1, transferAmount)));

        assertTrue(transferAmount < bank.getAccount(id1).getBalance());
        assertTrue(processor.handle(String.format("%s %s %s %s", transactionType, id1, id0, transferAmount)));

        assertEquals(400, bank.getAccount(id0).getBalance());
        assertEquals(1000, bank.getAccount(id1).getBalance());
    }

    @Test
    protected void transfer_processors_should_transfer_the_paying_account_balance_when_the_transfer_amount_is_greater_than_the_paying_account_balance() {
        String payingID = SAVINGS_ID_0;
        String receivingID = SAVINGS_ID_1;
        double transferAmount = 1000;

        bank.withdraw(SAVINGS_ID_0, transferAmount);
        bank.withdraw(SAVINGS_ID_0, transferAmount);

        assertTrue(transferAmount > bank.getAccount(payingID).getBalance());
        assertTrue(processor.handle(String.format("%s %s %s %s", transactionType, payingID, receivingID, transferAmount)));

        assertEquals(0, bank.getAccount(payingID).getBalance());
        assertEquals(3000, bank.getAccount(receivingID).getBalance());
    }

    @Test
    protected void transfer_processors_can_ignore_additional_arguments() {
        String payingID = CD_ID;
        String receivingID = SAVINGS_ID_1;
        double transferAmount = min(bank.getAccount(payingID).getMaxWithdrawAmount(), bank.getAccount(receivingID).getMaxDepositAmount());

        bank.timeTravel(12);

        assertTrue(processor.handle(String.format("%s %s %s %s %s", transactionType, payingID, receivingID, transferAmount, "nuke")));
        assertTrue(processor.handle(String.format("%s %s %s %s  %s  %s %s  %s   %s", transactionType, payingID, receivingID, transferAmount, "DsDifJ", "paSJiOf", "ps3f&jf", "sp@&HR*&HDSoa", "psd)(Jo")));
    }

    @Test
    protected void transfer_processors_are_case_insensitive() {
        String payingID = CHECKING_ID_1;
        String receivingID = CHECKING_ID_0;
        double transferAmount = min(bank.getAccount(payingID).getMaxWithdrawAmount(), bank.getAccount(receivingID).getMaxDepositAmount());

        assertTrue(processor.handle(String.format("traNSFer %s %s %s", payingID, receivingID, transferAmount)));
    }

    @Test
    protected void transfer_processors_can_be_in_a_chain_of_responsibility() {
        processor.setNext(new TimeTravelProcessor(bank));

        assertTrue(processor.handle(String.format("%s %s", TransactionType.TimeTravel, MONTHS)));
        assertEquals(timeTravel(checkingDepositAmount, MONTHS), bank.getAccount(CHECKING_ID_0).getBalance());
        assertEquals(timeTravel(checkingDepositAmount, MONTHS), bank.getAccount(CHECKING_ID_1).getBalance());
        assertEquals(timeTravel(savingsDepositAmount, MONTHS), bank.getAccount(SAVINGS_ID_0).getBalance());
        assertEquals(timeTravel(savingsDepositAmount, MONTHS), bank.getAccount(SAVINGS_ID_1).getBalance());
        assertEquals(timeTravel(cdBalance, MONTHS), bank.getAccount(CD_ID).getBalance());
        assertFalse(processor.handle(String.format("%s %s %s %s", TransactionType.Create, AccountType.CD, "73842793", cdBalance)));
    }
}
