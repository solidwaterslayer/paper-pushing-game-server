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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static server.game.pushing.paper.bank.BankTests.passTime;

public class TransferProcessorTests {
    protected TransferProcessor transferProcessor;
    protected Bank bank;

    protected final String CHECKING_ID_0 = "00000000";
    protected final String CHECKING_ID_1 = "10000000";
    protected final String SAVINGS_ID_0 = "00000001";
    protected final String SAVINGS_ID_1 = "10000001";
    protected final String CD_ID = "00000010";
    protected final double APR = 7;
    protected final double CHECKING_DEPOSIT_AMOUNT = 1000;
    protected final double SAVINGS_DEPOSIT_AMOUNT = 500;
    protected final double INITIAL_CD_BALANCE = 1000;

    @BeforeEach
    protected void setUp() {
        bank = new Bank(Arrays.asList(
                new Checking(CHECKING_ID_0, APR),
                new Checking(CHECKING_ID_1, APR),
                new Savings(SAVINGS_ID_0, APR),
                new Savings(SAVINGS_ID_1, APR),
                new CD(CD_ID, APR, INITIAL_CD_BALANCE)
        ));
        bank.deposit(CHECKING_ID_0, CHECKING_DEPOSIT_AMOUNT);
        bank.deposit(CHECKING_ID_1, CHECKING_DEPOSIT_AMOUNT);
        bank.deposit(SAVINGS_ID_0, SAVINGS_DEPOSIT_AMOUNT);
        bank.deposit(SAVINGS_ID_1, SAVINGS_DEPOSIT_AMOUNT);

        transferProcessor = new TransferProcessor(bank);
    }

    @Test
    protected void transfer_processor_when_transaction_can_not_process_should_pass_transaction_up_the_chain_of_responsibility() {
        double minBalanceFee = bank.getMinBalanceFee();
        int months = 60;

        transferProcessor.setNextHandler(new PassTimeProcessor(bank));
        transferProcessor.handle(String.format("pass time %d", months));

        assertEquals(passTime(APR, minBalanceFee, AccountType.Checking, CHECKING_DEPOSIT_AMOUNT, months), bank.getAccount(CHECKING_ID_0).getBalance());
        assertEquals(passTime(APR, minBalanceFee, AccountType.Checking, CHECKING_DEPOSIT_AMOUNT, months), bank.getAccount(CHECKING_ID_1).getBalance());
        assertEquals(passTime(APR, minBalanceFee, AccountType.Savings, SAVINGS_DEPOSIT_AMOUNT, months), bank.getAccount(SAVINGS_ID_0).getBalance());
        assertEquals(passTime(APR, minBalanceFee, AccountType.Savings, SAVINGS_DEPOSIT_AMOUNT, months), bank.getAccount(SAVINGS_ID_1).getBalance());
        assertEquals(passTime(APR, minBalanceFee, AccountType.CD, INITIAL_CD_BALANCE, months), bank.getAccount(CD_ID).getBalance());
        assertFalse(transferProcessor.handle("create cd 10 10000"));
    }

    @Test
    protected void transfer_from_checking_to_checking_transaction_should_process() {
        double transferAmount = 100;

        transferProcessor.handle(String.format("transfer %s %s %f", CHECKING_ID_0, CHECKING_ID_1, transferAmount));

        assertEquals(CHECKING_DEPOSIT_AMOUNT - transferAmount, bank.getAccount(CHECKING_ID_0).getBalance());
        assertEquals(CHECKING_DEPOSIT_AMOUNT + transferAmount, bank.getAccount(CHECKING_ID_1).getBalance());
    }

    @Test
    protected void transfer_from_checking_to_savings_transaction_should_process() {
        double transferAmount = 200;

        transferProcessor.handle(String.format("transfer %s %s %f", CHECKING_ID_0, SAVINGS_ID_0, transferAmount));

        assertEquals(CHECKING_DEPOSIT_AMOUNT - transferAmount, bank.getAccount(CHECKING_ID_0).getBalance());
        assertEquals(SAVINGS_DEPOSIT_AMOUNT + transferAmount, bank.getAccount(SAVINGS_ID_0).getBalance());
    }

    @Test
    protected void transfer_from_savings_to_checking_transaction_should_process() {
        double transferAmount = 300;

        transferProcessor.handle(String.format("transfer %s %s %f", SAVINGS_ID_1, CHECKING_ID_0, transferAmount));

        assertEquals(SAVINGS_DEPOSIT_AMOUNT - transferAmount, bank.getAccount(SAVINGS_ID_1).getBalance());
        assertEquals(CHECKING_DEPOSIT_AMOUNT + transferAmount, bank.getAccount(CHECKING_ID_0).getBalance());
    }

    @Test
    protected void transfer_from_savings_to_savings_transaction_should_process() {
        double transferAmount = 400;

        transferProcessor.handle(String.format("transfer %s %s %f", SAVINGS_ID_1, SAVINGS_ID_0, transferAmount));

        assertEquals(SAVINGS_DEPOSIT_AMOUNT - transferAmount, bank.getAccount(SAVINGS_ID_1).getBalance());
        assertEquals(SAVINGS_DEPOSIT_AMOUNT + transferAmount, bank.getAccount(SAVINGS_ID_0).getBalance());
    }

    @Test
    protected void transfer_from_cd_to_savings_transaction_should_process() {
        int months = 14;
        double transferAmount = 2000;

        bank.passTime(months);
        transferProcessor.handle(String.format("transfer %s %s %.20f", CD_ID, SAVINGS_ID_1, transferAmount));

        assertEquals(0, bank.getAccount(CD_ID).getBalance());
        assertEquals(passTime(APR, bank.getMinBalanceFee(), AccountType.Savings, SAVINGS_DEPOSIT_AMOUNT, months) + passTime(APR, bank.getMinBalanceFee(), AccountType.CD, INITIAL_CD_BALANCE, months), bank.getAccount(SAVINGS_ID_1).getBalance());
    }

    @Test
    protected void transfer_transaction_when_transaction_amount_is_less_than_or_equal_to_balance_should_process() {
        double checkingWithdrawAmount = 300;
        double savingsWithdrawAmount = SAVINGS_DEPOSIT_AMOUNT;

        bank.transfer(SAVINGS_ID_0, CHECKING_ID_1, savingsWithdrawAmount);
        bank.transfer(CHECKING_ID_1, SAVINGS_ID_0, checkingWithdrawAmount);

        assertEquals(SAVINGS_DEPOSIT_AMOUNT - savingsWithdrawAmount + checkingWithdrawAmount, bank.getAccount(SAVINGS_ID_0).getBalance());
        assertEquals(CHECKING_DEPOSIT_AMOUNT + savingsWithdrawAmount - checkingWithdrawAmount, bank.getAccount(CHECKING_ID_1).getBalance());
    }

    @Test
    protected void transfer_transaction_when_transfer_amount_is_greater_than_balance_should_transfer_amount_equal_to_balance() {
        double transferAmount = SAVINGS_DEPOSIT_AMOUNT + 500;

        bank.transfer(SAVINGS_ID_0, SAVINGS_ID_1, transferAmount);

        assertEquals(0, bank.getAccount(SAVINGS_ID_0).getBalance());
        assertEquals(SAVINGS_DEPOSIT_AMOUNT * 2, bank.getAccount(SAVINGS_ID_1).getBalance());
    }
}
