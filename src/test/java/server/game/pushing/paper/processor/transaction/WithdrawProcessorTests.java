package server.game.pushing.paper.processor.transaction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.game.pushing.paper.bank.Bank;
import server.game.pushing.paper.bank.account.AccountType;
import server.game.pushing.paper.bank.account.CD;
import server.game.pushing.paper.bank.account.Checking;
import server.game.pushing.paper.bank.account.Savings;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static server.game.pushing.paper.bank.BankTests.passTime;

public class WithdrawProcessorTests {
    protected WithdrawProcessor withdrawProcessor;
    protected Bank bank;
    protected final String CHECKING_ID = "00000000";
    protected final String SAVINGS_ID = "00000001";
    protected final String CD_ID = "00000010";
    protected final double APR = 8;
    protected final double CHECKING_DEPOSIT_AMOUNT = 300;
    protected final double SAVINGS_DEPOSIT_AMOUNT = 700;
    protected final double INITIAL_CD_BALANCE = 7000;

    @BeforeEach
    void setUp() {
        bank = new Bank(Arrays.asList(
                new Checking(CHECKING_ID, APR),
                new Savings(SAVINGS_ID, APR),
                new CD(CD_ID, APR, INITIAL_CD_BALANCE)
        ));
        bank.deposit(CHECKING_ID, CHECKING_DEPOSIT_AMOUNT);
        bank.deposit(SAVINGS_ID, SAVINGS_DEPOSIT_AMOUNT);

        withdrawProcessor = new WithdrawProcessor(bank);
    }

    @Test
    protected void withdraw_processor_when_transaction_can_not_process_should_pass_transaction_up_the_chain_of_responsibility() {
        double transferAmount = 400;

        withdrawProcessor.setNextHandler(new TransferProcessor(bank));
        assertTrue(withdrawProcessor.handle(String.format("transfer %s %s %f", SAVINGS_ID, CHECKING_ID, transferAmount)));

        assertEquals(SAVINGS_DEPOSIT_AMOUNT - transferAmount, bank.getAccount(SAVINGS_ID).getBalance());
        assertEquals(CHECKING_DEPOSIT_AMOUNT + transferAmount, bank.getAccount(CHECKING_ID).getBalance());
        assertFalse(withdrawProcessor.handle("pass time 60"));
    }

    @Test
    protected void withdraw_checking_transaction_when_withdraw_amount_is_less_than_balance_should_process() {
        double checkingWithdrawAmount = 200;

        withdrawProcessor.handle(String.format("withdraw %s %f", CHECKING_ID, checkingWithdrawAmount));

        assertEquals(CHECKING_DEPOSIT_AMOUNT - checkingWithdrawAmount, bank.getAccount(CHECKING_ID).getBalance());
    }

    @Test
    protected void withdraw_savings_transaction_when_withdraw_amount_is_less_than_balance_should_process() {
        double savingsWithdrawAmount = 300;

        withdrawProcessor.handle(String.format("withdraw %s %f", SAVINGS_ID, savingsWithdrawAmount));

        assertEquals(SAVINGS_DEPOSIT_AMOUNT - savingsWithdrawAmount, bank.getAccount(SAVINGS_ID).getBalance());
    }

    @Test
    protected void withdraw_transaction_when_withdraw_amount_is_equal_to_balance_should_process() {
        double minBalanceFee = bank.getMinBalanceFee();
        int months = 12;
        double checkingWithdrawAmount = passTime(APR, minBalanceFee, AccountType.Checking, CHECKING_DEPOSIT_AMOUNT, months);
        double savingsWithdrawAmount = passTime(APR, minBalanceFee, AccountType.Savings, SAVINGS_DEPOSIT_AMOUNT, months);
        double cdWithdrawAmount = passTime(APR, minBalanceFee, AccountType.CD, INITIAL_CD_BALANCE, months);

        bank.passTime(months);

        assertEquals(checkingWithdrawAmount, bank.getAccount(CHECKING_ID).getBalance());
        withdrawProcessor.handle(String.format("withdraw %s %.20f", CHECKING_ID, checkingWithdrawAmount));

        assertEquals(savingsWithdrawAmount, bank.getAccount(SAVINGS_ID).getBalance());
        withdrawProcessor.handle(String.format("withdraw %s %.20f", SAVINGS_ID, savingsWithdrawAmount));

        assertEquals(cdWithdrawAmount, bank.getAccount(CD_ID).getBalance());
        withdrawProcessor.handle(String.format("withdraw %s %.20f", CD_ID, cdWithdrawAmount));

        assertEquals(0, bank.getAccount(CHECKING_ID).getBalance());
        assertEquals(0, bank.getAccount(SAVINGS_ID).getBalance());
        assertEquals(0, bank.getAccount(CD_ID).getBalance());
    }

    @Test
    protected void withdraw_transaction_when_withdraw_amount_is_greater_than_balance_should_withdraw_amount_equal_to_balance() {
        double checkingWithdrawAmount = 400;
        double savingsWithdrawAmount = 1000;
        double cdWithdrawAmount = INITIAL_CD_BALANCE * 2;

        bank.passTime(12);

        assertTrue(checkingWithdrawAmount > bank.getAccount(CHECKING_ID).getBalance());
        withdrawProcessor.handle(String.format("withdraw %s %f", CHECKING_ID, checkingWithdrawAmount));

        assertTrue(savingsWithdrawAmount > bank.getAccount(SAVINGS_ID).getBalance());
        withdrawProcessor.handle(String.format("withdraw %s %f", SAVINGS_ID, savingsWithdrawAmount));

        assertTrue(cdWithdrawAmount > bank.getAccount(CD_ID).getBalance());
        withdrawProcessor.handle(String.format("withdraw %s %f", CD_ID, cdWithdrawAmount));

        assertEquals(0, bank.getAccount(CHECKING_ID).getBalance());
        assertEquals(0, bank.getAccount(SAVINGS_ID).getBalance());
        assertEquals(0, bank.getAccount(CD_ID).getBalance());
    }
}
