package server.game.pushing.paper.store.chain_of_responsibility.transaction_processor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.game.pushing.paper.store.bank.Bank;
import server.game.pushing.paper.store.chain_of_responsibility.ChainOfResponsibility;
import server.game.pushing.paper.store.chain_of_responsibility.TransactionType;

import static java.lang.Math.min;
import static org.junit.jupiter.api.Assertions.*;
import static server.game.pushing.paper.store.bank.Bank.getMonthsPerYear;
import static server.game.pushing.paper.store.bank.BankTests.timeTravel;

public class WithdrawProcessorTests {
    private Bank bank;
    private ChainOfResponsibility processor;

    private final int MONTHS = getMonthsPerYear();
    private TransactionType transactionType;
    private final String CHECKING_ID = "87439752";
    private final String SAVINGS_ID = "09329843";
    private final String CD_ID = "43894280";
    private double startingCDBalance;
    private double checkingDepositAmount;
    private double savingsDepositAmount;

    @BeforeEach
    void setUp() {
        bank = new Bank();
        processor = new WithdrawProcessor(bank);

        transactionType = processor.getTransactionType();
        startingCDBalance = bank.getMinStartingCDBalance();
        bank.createChecking(CHECKING_ID);
        bank.createSavings(SAVINGS_ID);
        bank.createCD(CD_ID, startingCDBalance);
        checkingDepositAmount = bank.getAccount(CHECKING_ID).getMaxDepositAmount();
        savingsDepositAmount = bank.getAccount(SAVINGS_ID).getMaxDepositAmount();

        bank.deposit(CHECKING_ID, checkingDepositAmount);
        bank.deposit(SAVINGS_ID, savingsDepositAmount);
    }

    @Test
    protected void withdraw_processors_can_withdraw_from_checking_accounts_when_the_withdraw_amount_is_less_than_the_account_balance() {
        String id = CHECKING_ID;
        double withdrawAmount = bank.getAccount(id).getMaxWithdrawAmount();

        assertTrue(processor.handle(String.format("%s %s %s", transactionType, id, withdrawAmount)));
        assertTrue(withdrawAmount < checkingDepositAmount);
        assertEquals(checkingDepositAmount - withdrawAmount, bank.getAccount(id).getBalance());
    }

    @Test
    protected void withdraw_processors_can_withdraw_from_savings_accounts_when_the_withdraw_amount_is_less_than_the_account_balance() {
        String id = SAVINGS_ID;
        double withdrawAmount = bank.getAccount(id).getMaxWithdrawAmount();

        assertTrue(processor.handle(String.format("%s %s %s", transactionType, id, withdrawAmount)));
        assertTrue(withdrawAmount < savingsDepositAmount);
        assertEquals(savingsDepositAmount - withdrawAmount, bank.getAccount(id).getBalance());
    }

    @Test
    protected void withdraw_processors_can_withdraw_when_the_withdraw_amount_is_equal_to_the_account_balance() {
        double checkingWithdrawAmount = timeTravel(bank, MONTHS, checkingDepositAmount);
        double savingsWithdrawAmount = timeTravel(bank, MONTHS, savingsDepositAmount);
        double cdWithdrawAmount = timeTravel(bank, MONTHS, startingCDBalance);
        bank.timeTravel(MONTHS);

        assertEquals(checkingWithdrawAmount, bank.getAccount(CHECKING_ID).getBalance());
        assertTrue(processor.handle(String.format("%s %s %s", transactionType, CHECKING_ID, checkingWithdrawAmount)));
        assertEquals(0, bank.getAccount(CHECKING_ID).getBalance());

        assertEquals(savingsWithdrawAmount, bank.getAccount(SAVINGS_ID).getBalance());
        assertTrue(processor.handle(String.format("%s %s %s", transactionType, SAVINGS_ID, savingsWithdrawAmount)));
        assertEquals(0, bank.getAccount(SAVINGS_ID).getBalance());

        assertEquals(cdWithdrawAmount, bank.getAccount(CD_ID).getBalance());
        assertTrue(processor.handle(String.format("%s %s %s", transactionType, CD_ID, cdWithdrawAmount)));
        assertEquals(0, bank.getAccount(CD_ID).getBalance());
    }

    @Test
    protected void withdraw_processors_can_withdraw_when_the_withdraw_amount_is_greater_than_the_account_balance() {
        double checkingWithdrawAmount = bank.getAccount(CHECKING_ID).getMaxWithdrawAmount();
        double savingsWithdrawAmount = bank.getAccount(SAVINGS_ID).getMaxWithdrawAmount();
        double cdWithdrawAmount = bank.getAccount(CD_ID).getMaxWithdrawAmount();
        bank.withdraw(CHECKING_ID, checkingWithdrawAmount);
        bank.withdraw(CHECKING_ID, checkingWithdrawAmount);
        bank.withdraw(SAVINGS_ID, savingsWithdrawAmount);
        bank.withdraw(SAVINGS_ID, savingsWithdrawAmount);
        bank.timeTravel(12);

        assertTrue(checkingWithdrawAmount > bank.getAccount(CHECKING_ID).getBalance());
        assertTrue(processor.handle(String.format("%s %s %s", transactionType, CHECKING_ID, checkingWithdrawAmount)));
        assertEquals(0, bank.getAccount(CHECKING_ID).getBalance());

        assertTrue(savingsWithdrawAmount > bank.getAccount(SAVINGS_ID).getBalance());
        assertTrue(processor.handle(String.format("%s %s %s", transactionType, SAVINGS_ID, savingsWithdrawAmount)));
        assertEquals(0, bank.getAccount(SAVINGS_ID).getBalance());

        assertTrue(cdWithdrawAmount > bank.getAccount(CD_ID).getBalance());
        assertTrue(processor.handle(String.format("%s %s %s", transactionType, CD_ID, cdWithdrawAmount)));
        assertEquals(0, bank.getAccount(CD_ID).getBalance());
    }

    @Test
    protected void withdraw_processors_can_ignore_additional_arguments() {
        bank.timeTravel(MONTHS);

        assertTrue(processor.handle(String.format("%s %s %s %s", transactionType, CHECKING_ID, bank.getAccount(CHECKING_ID).getMaxWithdrawAmount(), "nuke")));
        assertTrue(processor.handle(String.format("%s %s %s %s %s  %s    %s         %s", transactionType, SAVINGS_ID, bank.getAccount(SAVINGS_ID).getMaxWithdrawAmount(), "00", "000", "00000", "000", 0)));
        assertTrue(processor.handle(String.format("%s %s %s %s %s %s", transactionType, CD_ID, bank.getAccount(CD_ID).getMaxWithdrawAmount(), "d", "e", "r")));
    }

    @Test
    protected void withdraw_processors_are_case_insensitive() {
        bank.timeTravel(MONTHS);

        assertTrue(processor.handle(String.format("withdraw %s %s", CHECKING_ID, bank.getAccount(CHECKING_ID).getMaxWithdrawAmount())));
        assertTrue(processor.handle(String.format("wITHdrAw %s %s", SAVINGS_ID, bank.getAccount(SAVINGS_ID).getMaxWithdrawAmount())));
        assertTrue(processor.handle(String.format("WITHDRAW %s %s", CD_ID, bank.getAccount(CD_ID).getMaxWithdrawAmount())));
    }

    @Test
    protected void withdraw_processors_can_be_in_a_chain_of_responsibility() {
        String payingID = SAVINGS_ID;
        String receivingID = CHECKING_ID;
        double transferAmount = min(bank.getAccount(payingID).getMaxWithdrawAmount(), bank.getAccount(receivingID).getMaxDepositAmount());

        processor.setNext(new TransferProcessor(bank));

        assertTrue(processor.handle(String.format("%s %s %s %s", TransactionType.Transfer, payingID, receivingID, transferAmount)));
        assertEquals(savingsDepositAmount - transferAmount, bank.getAccount(payingID).getBalance());
        assertEquals(checkingDepositAmount + transferAmount, bank.getAccount(receivingID).getBalance());
        assertFalse(processor.handle(String.format("%s %s", TransactionType.TimeTravel, MONTHS)));
    }
}
