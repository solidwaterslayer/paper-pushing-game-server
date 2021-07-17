package server.game.pushing.paper.processor.transaction;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.game.pushing.paper.bank.Bank;
import server.game.pushing.paper.bank.account.AccountType;
import server.game.pushing.paper.bank.account.CD;
import server.game.pushing.paper.bank.account.Checking;
import server.game.pushing.paper.bank.account.Savings;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static server.game.pushing.paper.bank.BankTests.passTime;

public class WithdrawProcessorTests {
    protected WithdrawProcessor withdrawProcessor;
    protected Bank bank;
    protected final String CHECKING_ID = "00000000";
    protected final String SAVINGS_ID = "00000001";
    protected final String CD_ID = "00000010";
    protected final double APR = 0.1;
    protected final double CHECKING_DEPOSIT_AMOUNT = 500;
    protected final double SAVINGS_DEPOSIT_AMOUNT = 700;
    protected final double INITIAL_CD_BALANCE = 700;

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
        withdrawProcessor.handle(String.format("transfer %s %s %f", SAVINGS_ID, CHECKING_ID, transferAmount));

        assertEquals(SAVINGS_DEPOSIT_AMOUNT - transferAmount, bank.getAccount(SAVINGS_ID).getBalance());
        assertEquals(CHECKING_DEPOSIT_AMOUNT + transferAmount, bank.getAccount(CHECKING_ID).getBalance());
        assertFalse(withdrawProcessor.handle("pass time 60"));
    }

    @Test
    void withdraw_transaction_should_be_process() {
        double checkingWithdrawAmount = 200;
        double savingsWithdrawAmount = 300;
        double cdWithdrawAmount = 2000;

        bank.passTime(12);
        withdrawProcessor.handle(String.format("withdraw %s %f", CHECKING_ID, checkingWithdrawAmount));
        withdrawProcessor.handle(String.format("withdraw %s %f", SAVINGS_ID, savingsWithdrawAmount));
        withdrawProcessor.handle(String.format("withdraw %s %f", CD_ID, cdWithdrawAmount));

        assertEquals(passTime(APR, bank.getMinBalanceFee(), AccountType.Checking, CHECKING_DEPOSIT_AMOUNT, 12) - checkingWithdrawAmount, bank.getAccount(CHECKING_ID).getBalance());
        assertEquals(passTime(APR, bank.getMinBalanceFee(), AccountType.Savings, SAVINGS_DEPOSIT_AMOUNT, 12) - savingsWithdrawAmount, bank.getAccount(SAVINGS_ID).getBalance());
        assertEquals(0, bank.getAccount(CD_ID).getBalance());
    }
}
