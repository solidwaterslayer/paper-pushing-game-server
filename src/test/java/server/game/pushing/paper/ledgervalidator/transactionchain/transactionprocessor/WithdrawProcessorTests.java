package server.game.pushing.paper.ledgervalidator.transactionchain.transactionprocessor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.game.pushing.paper.ledgervalidator.bank.Bank;
import server.game.pushing.paper.ledgervalidator.bank.BankTests;
import server.game.pushing.paper.ledgervalidator.bank.account.AccountType;
import server.game.pushing.paper.ledgervalidator.bank.account.CD;
import server.game.pushing.paper.ledgervalidator.bank.account.Checking;
import server.game.pushing.paper.ledgervalidator.bank.account.Savings;
import server.game.pushing.paper.ledgervalidator.transactionchain.TransactionType;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static server.game.pushing.paper.ledgervalidator.bank.Bank.*;

public class WithdrawProcessorTests {
    protected Bank bank;
    protected WithdrawProcessor withdrawProcessor;

    protected final String CHECKING_ID = "87439752";
    protected final String SAVINGS_ID = "09329843";
    protected final String CD_ID = "43894280";
    protected final double APR = getMaxAPR();
    protected final double CHECKING_DEPOSIT_AMOUNT = Checking.getMaxDepositAmount();
    protected final double SAVINGS_DEPOSIT_AMOUNT = Savings.getMaxDepositAmount();
    protected final double INITIAL_CD_BALANCE = getMinInitialCDBalance();

    @BeforeEach
    void setUp() {
        bank = new Bank(Arrays.asList(
                new Checking(CHECKING_ID, APR),
                new Savings(SAVINGS_ID, APR),
                new CD(CD_ID, APR, INITIAL_CD_BALANCE)
        ));
        withdrawProcessor = new WithdrawProcessor(bank);

        bank.deposit(CHECKING_ID, CHECKING_DEPOSIT_AMOUNT);
        bank.deposit(SAVINGS_ID, SAVINGS_DEPOSIT_AMOUNT);
    }

    @Test
    protected void withdraw_processor_when_transaction_can_not_process_should_pass_transaction_up_the_chain_of_responsibility() {
        String fromID = SAVINGS_ID;
        String toID = CHECKING_ID;
        double transferAmount = 400;
        int months = getMonthsPerYear();

        withdrawProcessor.setNext(new TransferProcessor(bank));

        assertTrue(withdrawProcessor.handle(String.format("%s %s %s %s", TransactionType.Transfer, fromID, toID, transferAmount)));
        assertEquals(SAVINGS_DEPOSIT_AMOUNT - transferAmount, bank.getAccount(fromID).getBalance());
        assertEquals(CHECKING_DEPOSIT_AMOUNT + transferAmount, bank.getAccount(toID).getBalance());
        assertFalse(withdrawProcessor.handle(String.format("%s %s", TransactionType.PassTime, months)));
    }

    @Test
    protected void withdraw_checking_transaction_when_withdraw_amount_is_less_than_balance_should_process() {
        TransactionType transactionType = TransactionType.Withdraw;
        String id = CHECKING_ID;
        double withdrawAmount = Checking.getMaxWithdrawAmount();

        assertTrue(withdrawProcessor.handle(String.format("%s %s %s", transactionType, id, withdrawAmount)));
        assertTrue(withdrawAmount < CHECKING_DEPOSIT_AMOUNT);
        assertEquals(CHECKING_DEPOSIT_AMOUNT - withdrawAmount, bank.getAccount(id).getBalance());
    }

    @Test
    protected void withdraw_savings_transaction_when_withdraw_amount_is_less_than_balance_should_process() {
        TransactionType transactionType = TransactionType.Withdraw;
        String id = SAVINGS_ID;
        double withdrawAmount = Savings.getMaxWithdrawAmount();

        assertTrue(withdrawProcessor.handle(String.format("%s %s %s", transactionType, id, withdrawAmount)));
        assertTrue(withdrawAmount < SAVINGS_DEPOSIT_AMOUNT);
        assertEquals(SAVINGS_DEPOSIT_AMOUNT - withdrawAmount, bank.getAccount(id).getBalance());
    }

    @Test
    protected void withdraw_transaction_when_withdraw_amount_is_equal_to_balance_should_process() {
        TransactionType transactionType = TransactionType.Withdraw;
        double minBalanceFee = bank.getMinBalanceFee();
        int months = getMonthsPerYear();
        double checkingWithdrawAmount = BankTests.passTime(APR, minBalanceFee, AccountType.Checking, CHECKING_DEPOSIT_AMOUNT, months);
        double savingsWithdrawAmount = BankTests.passTime(APR, minBalanceFee, AccountType.Savings, SAVINGS_DEPOSIT_AMOUNT, months);
        double cdWithdrawAmount = BankTests.passTime(APR, minBalanceFee, AccountType.CD, INITIAL_CD_BALANCE, months);
        bank.passTime(months);

        assertEquals(checkingWithdrawAmount, bank.getAccount(CHECKING_ID).getBalance());
        assertTrue(withdrawProcessor.handle(String.format("%s %s %s", transactionType, CHECKING_ID, checkingWithdrawAmount)));
        assertEquals(0, bank.getAccount(CHECKING_ID).getBalance());

        assertEquals(savingsWithdrawAmount, bank.getAccount(SAVINGS_ID).getBalance());
        assertTrue(withdrawProcessor.handle(String.format("%s %s %s", transactionType, SAVINGS_ID, savingsWithdrawAmount)));
        assertEquals(0, bank.getAccount(SAVINGS_ID).getBalance());

        assertEquals(cdWithdrawAmount, bank.getAccount(CD_ID).getBalance());
        assertTrue(withdrawProcessor.handle(String.format("%s %s %s", transactionType, CD_ID, cdWithdrawAmount)));
        assertEquals(0, bank.getAccount(CD_ID).getBalance());
    }

    @Test
    protected void withdraw_transaction_when_withdraw_amount_is_greater_than_balance_should_withdraw_amount_equal_to_balance() {
        TransactionType transactionType = TransactionType.Withdraw;
        double checkingWithdrawAmount = Checking.getMaxWithdrawAmount();
        double savingsWithdrawAmount = Savings.getMaxWithdrawAmount();
        double cdWithdrawAmount = CD.getMaxWithdrawAmount();
        bank.withdraw(CHECKING_ID, checkingWithdrawAmount);
        bank.withdraw(CHECKING_ID, checkingWithdrawAmount);
        bank.withdraw(SAVINGS_ID, savingsWithdrawAmount);
        bank.withdraw(SAVINGS_ID, savingsWithdrawAmount);
        bank.passTime(12);

        assertTrue(checkingWithdrawAmount > bank.getAccount(CHECKING_ID).getBalance());
        assertTrue(withdrawProcessor.handle(String.format("%s %s %s", transactionType, CHECKING_ID, checkingWithdrawAmount)));
        assertEquals(0, bank.getAccount(CHECKING_ID).getBalance());

        assertTrue(savingsWithdrawAmount > bank.getAccount(SAVINGS_ID).getBalance());
        assertTrue(withdrawProcessor.handle(String.format("%s %s %s", transactionType, SAVINGS_ID, savingsWithdrawAmount)));
        assertEquals(0, bank.getAccount(SAVINGS_ID).getBalance());

        assertTrue(cdWithdrawAmount > bank.getAccount(CD_ID).getBalance());
        assertTrue(withdrawProcessor.handle(String.format("%s %s %s", transactionType, CD_ID, cdWithdrawAmount)));
        assertEquals(0, bank.getAccount(CD_ID).getBalance());
    }
}
