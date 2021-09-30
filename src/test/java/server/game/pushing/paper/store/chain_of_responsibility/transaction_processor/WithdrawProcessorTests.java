package server.game.pushing.paper.store.chain_of_responsibility.transaction_processor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.game.pushing.paper.store.bank.Bank;
import server.game.pushing.paper.store.bank.account.AccountType;
import server.game.pushing.paper.store.chain_of_responsibility.ChainOfResponsibility;
import server.game.pushing.paper.store.chain_of_responsibility.TransactionType;

import static java.lang.Math.min;
import static org.junit.jupiter.api.Assertions.*;
import static server.game.pushing.paper.store.bank.Bank.getMonthsPerYear;
import static server.game.pushing.paper.store.bank.BankTests.passTime;

public class WithdrawProcessorTests {
    private Bank bank;
    private ChainOfResponsibility processor;

    private double minBalanceFee;
    private final int MONTHS = getMonthsPerYear();
    private TransactionType transactionType;
    private final String CHECKING_ID = "87439752";
    private final String SAVINGS_ID = "09329843";
    private final String CD_ID = "43894280";
    private double apr;
    private double initialCDBalance;
    private double checkingDepositAmount;
    private double savingsDepositAmount;

    @BeforeEach
    void setUp() {
        bank = new Bank();
        processor = new WithdrawProcessor(bank);

        minBalanceFee = bank.getMinBalanceFee();
        transactionType = processor.getTransactionType();
        apr = bank.getMaxAPR();
        initialCDBalance = bank.getMinInitialCDBalance();
        bank.createChecking(CHECKING_ID, apr);
        bank.createSavings(SAVINGS_ID, apr);
        bank.createCD(CD_ID, apr, initialCDBalance);
        checkingDepositAmount = bank.getAccount(CHECKING_ID).getMaxDepositAmount();
        savingsDepositAmount = bank.getAccount(SAVINGS_ID).getMaxDepositAmount();

        bank.deposit(CHECKING_ID, checkingDepositAmount);
        bank.deposit(SAVINGS_ID, savingsDepositAmount);
    }

    @Test
    protected void withdraw_processor_when_transaction_can_not_process_should_pass_transaction_down_the_chain_of_responsibility() {
        String fromID = SAVINGS_ID;
        String toID = CHECKING_ID;
        double transferAmount = min(bank.getAccount(fromID).getMaxWithdrawAmount(), bank.getAccount(toID).getMaxDepositAmount());

        processor.setNext(new TransferProcessor(bank));

        assertTrue(processor.handle(String.format("%s %s %s %s", TransactionType.Transfer, fromID, toID, transferAmount)));
        assertEquals(savingsDepositAmount - transferAmount, bank.getAccount(fromID).getBalance());
        assertEquals(checkingDepositAmount + transferAmount, bank.getAccount(toID).getBalance());
        assertFalse(processor.handle(String.format("%s %s", TransactionType.PassTime, MONTHS)));
    }

    @Test
    protected void withdraw_checking_transaction_when_withdraw_amount_is_less_than_balance_should_process() {
        String id = CHECKING_ID;
        double withdrawAmount = bank.getAccount(id).getMaxWithdrawAmount();

        assertTrue(processor.handle(String.format("%s %s %s", transactionType, id, withdrawAmount)));
        assertTrue(withdrawAmount < checkingDepositAmount);
        assertEquals(checkingDepositAmount - withdrawAmount, bank.getAccount(id).getBalance());
    }

    @Test
    protected void withdraw_savings_transaction_when_withdraw_amount_is_less_than_balance_should_process() {
        String id = SAVINGS_ID;
        double withdrawAmount = bank.getAccount(id).getMaxWithdrawAmount();

        assertTrue(processor.handle(String.format("%s %s %s", transactionType, id, withdrawAmount)));
        assertTrue(withdrawAmount < savingsDepositAmount);
        assertEquals(savingsDepositAmount - withdrawAmount, bank.getAccount(id).getBalance());
    }

    @Test
    protected void transaction_when_withdraw_amount_is_equal_to_balance_should_process() {
        double checkingWithdrawAmount = passTime(minBalanceFee, MONTHS, AccountType.CHECKING, apr, checkingDepositAmount);
        double savingsWithdrawAmount = passTime(minBalanceFee, MONTHS, AccountType.SAVINGS, apr, savingsDepositAmount);
        double cdWithdrawAmount = passTime(minBalanceFee, MONTHS, AccountType.CD, apr, initialCDBalance);
        bank.passTime(MONTHS);

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
    protected void transaction_when_withdraw_amount_is_greater_than_balance_should_withdraw_amount_equal_to_balance() {
        double checkingWithdrawAmount = bank.getAccount(CHECKING_ID).getMaxWithdrawAmount();
        double savingsWithdrawAmount = bank.getAccount(SAVINGS_ID).getMaxWithdrawAmount();
        double cdWithdrawAmount = bank.getAccount(CD_ID).getMaxWithdrawAmount();
        bank.withdraw(CHECKING_ID, checkingWithdrawAmount);
        bank.withdraw(CHECKING_ID, checkingWithdrawAmount);
        bank.withdraw(SAVINGS_ID, savingsWithdrawAmount);
        bank.withdraw(SAVINGS_ID, savingsWithdrawAmount);
        bank.passTime(12);

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
    protected void transaction_should_be_case_insensitive() {
        bank.passTime(MONTHS);

        assertTrue(processor.handle(String.format("withdraw %s %s", CHECKING_ID, bank.getAccount(CHECKING_ID).getMaxWithdrawAmount())));
        assertTrue(processor.handle(String.format("wITHdrAw %s %s", SAVINGS_ID, bank.getAccount(SAVINGS_ID).getMaxWithdrawAmount())));
        assertTrue(processor.handle(String.format("WITHDRAW %s %s", CD_ID, bank.getAccount(CD_ID).getMaxWithdrawAmount())));
    }

    @Test
    protected void transaction_should_be_possible_with_useless_additional_arguments() {
        bank.passTime(MONTHS);

        assertTrue(processor.handle(String.format("%s %s %s %s", transactionType, CHECKING_ID, bank.getAccount(CHECKING_ID).getMaxWithdrawAmount(), "nuke")));
        assertTrue(processor.handle(String.format("%s %s %s %s %s  %s    %s         %s", transactionType, SAVINGS_ID, bank.getAccount(SAVINGS_ID).getMaxWithdrawAmount(), "00", "000", "00000", "000", 0)));
        assertTrue(processor.handle(String.format("%s %s %s %s %s %s", transactionType, CD_ID, bank.getAccount(CD_ID).getMaxWithdrawAmount(), "d", "e", "r")));
    }
}
