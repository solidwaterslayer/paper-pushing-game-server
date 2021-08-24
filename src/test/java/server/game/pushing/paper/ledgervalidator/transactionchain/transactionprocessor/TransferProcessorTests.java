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

import static java.lang.Math.min;
import static org.junit.jupiter.api.Assertions.*;
import static server.game.pushing.paper.ledgervalidator.bank.Bank.*;

public class TransferProcessorTests {
    protected Bank bank;
    protected TransferProcessor transferProcessor;

    protected final String CHECKING_ID_0 = "98830842";
    protected final String CHECKING_ID_1 = "09309843";
    protected final String SAVINGS_ID_0 = "90328934";
    protected final String SAVINGS_ID_1 = "11117823";
    protected final String CD_ID = "08429834";
    protected final double APR = getMaxAPR();
    protected final double CHECKING_DEPOSIT_AMOUNT = Checking.getMaxDepositAmount();
    protected final double SAVINGS_DEPOSIT_AMOUNT = Savings.getMaxDepositAmount();
    protected final double INITIAL_CD_BALANCE = getMinInitialCDBalance();

    @BeforeEach
    protected void setUp() {
        bank = new Bank(Arrays.asList(
                new Checking(CHECKING_ID_0, APR),
                new Checking(CHECKING_ID_1, APR),
                new Savings(SAVINGS_ID_0, APR),
                new Savings(SAVINGS_ID_1, APR),
                new CD(CD_ID, APR, INITIAL_CD_BALANCE)
        ));
        transferProcessor = new TransferProcessor(bank);

        bank.deposit(CHECKING_ID_0, CHECKING_DEPOSIT_AMOUNT);
        bank.deposit(CHECKING_ID_1, CHECKING_DEPOSIT_AMOUNT);
        bank.deposit(SAVINGS_ID_0, SAVINGS_DEPOSIT_AMOUNT);
        bank.deposit(SAVINGS_ID_1, SAVINGS_DEPOSIT_AMOUNT);
    }

    @Test
    protected void transfer_processor_when_transaction_can_not_process_should_pass_transaction_up_the_chain_of_responsibility() {
        double minBalanceFee = bank.getMinBalanceFee();
        int months = getMonthsPerYear();

        transferProcessor.setNext(new PassTimeProcessor(bank));

        assertTrue(transferProcessor.handle(String.format("%s %s", TransactionType.PassTime, months)));
        assertEquals(BankTests.passTime(APR, minBalanceFee, AccountType.Checking, CHECKING_DEPOSIT_AMOUNT, months), bank.getAccount(CHECKING_ID_0).getBalance());
        assertEquals(BankTests.passTime(APR, minBalanceFee, AccountType.Checking, CHECKING_DEPOSIT_AMOUNT, months), bank.getAccount(CHECKING_ID_1).getBalance());
        assertEquals(BankTests.passTime(APR, minBalanceFee, AccountType.Savings, SAVINGS_DEPOSIT_AMOUNT, months), bank.getAccount(SAVINGS_ID_0).getBalance());
        assertEquals(BankTests.passTime(APR, minBalanceFee, AccountType.Savings, SAVINGS_DEPOSIT_AMOUNT, months), bank.getAccount(SAVINGS_ID_1).getBalance());
        assertEquals(BankTests.passTime(APR, minBalanceFee, AccountType.CD, INITIAL_CD_BALANCE, months), bank.getAccount(CD_ID).getBalance());
        assertFalse(transferProcessor.handle(String.format("%s %s %s %s %s", TransactionType.Create, AccountType.CD, "73842793", APR, INITIAL_CD_BALANCE)));
    }

    @Test
    protected void transfer_from_checking_to_checking_transaction_should_process() {
        TransactionType transactionType = TransactionType.Transfer;
        String fromID = CHECKING_ID_0;
        String toID = CHECKING_ID_1;
        double transferAmount = min(Checking.getMaxWithdrawAmount(), Checking.getMaxDepositAmount());

        assertTrue(transferProcessor.handle(String.format("%s %s %s %s", transactionType, fromID, toID, transferAmount)));
        assertEquals(CHECKING_DEPOSIT_AMOUNT - transferAmount, bank.getAccount(fromID).getBalance());
        assertEquals(CHECKING_DEPOSIT_AMOUNT + transferAmount, bank.getAccount(toID).getBalance());
    }

    @Test
    protected void transfer_from_checking_to_savings_transaction_should_process() {
        TransactionType transactionType = TransactionType.Transfer;
        String fromID = CHECKING_ID_0;
        String toID = SAVINGS_ID_0;
        double transferAmount = min(Checking.getMaxWithdrawAmount(), Savings.getMaxDepositAmount());

        assertTrue(transferProcessor.handle(String.format("%s %s %s %s", transactionType, fromID, toID, transferAmount)));
        assertEquals(CHECKING_DEPOSIT_AMOUNT - transferAmount, bank.getAccount(fromID).getBalance());
        assertEquals(SAVINGS_DEPOSIT_AMOUNT + transferAmount, bank.getAccount(toID).getBalance());
    }

    @Test
    protected void transfer_from_savings_to_checking_transaction_should_process() {
        TransactionType transactionType = TransactionType.Transfer;
        String fromID = SAVINGS_ID_1;
        String toID = CHECKING_ID_0;
        double transferAmount = min(Savings.getMaxWithdrawAmount(), Checking.getMaxDepositAmount());

        assertTrue(transferProcessor.handle(String.format("%s %s %s %s", transactionType, fromID, toID, transferAmount)));
        assertEquals(SAVINGS_DEPOSIT_AMOUNT - transferAmount, bank.getAccount(fromID).getBalance());
        assertEquals(CHECKING_DEPOSIT_AMOUNT + transferAmount, bank.getAccount(toID).getBalance());
    }

    @Test
    protected void transfer_from_savings_to_savings_transaction_should_process() {
        TransactionType transactionType = TransactionType.Transfer;
        String fromID = SAVINGS_ID_1;
        String toID = SAVINGS_ID_0;
        double transferAmount = min(Savings.getMaxWithdrawAmount(), Savings.getMaxDepositAmount());

        assertTrue(transferProcessor.handle(String.format("%s %s %s %s", transactionType, fromID, toID, transferAmount)));
        assertEquals(SAVINGS_DEPOSIT_AMOUNT - transferAmount, bank.getAccount(fromID).getBalance());
        assertEquals(SAVINGS_DEPOSIT_AMOUNT + transferAmount, bank.getAccount(toID).getBalance());
    }

    @Test
    protected void transfer_from_cd_to_savings_transaction_should_process() {
        int months = getMonthsPerYear();
        double minBalanceFee = bank.getMinBalanceFee();
        TransactionType transactionType = TransactionType.Transfer;
        String fromID = CD_ID;
        String toID = SAVINGS_ID_1;
        double transferAmount = min(CD.getMaxWithdrawAmount(), Savings.getMaxDepositAmount());

        bank.passTime(months);

        assertTrue(transferProcessor.handle(String.format("%s %s %s %s", transactionType, fromID, toID, transferAmount)));
        assertEquals(0, bank.getAccount(fromID).getBalance());
        assertEquals(
                BankTests.passTime(APR, minBalanceFee, AccountType.Savings, SAVINGS_DEPOSIT_AMOUNT, months)
                        + BankTests.passTime(APR, minBalanceFee, AccountType.CD, INITIAL_CD_BALANCE, months)
                , bank.getAccount(toID).getBalance()
        );
    }

    @Test
    protected void transfer_transaction_when_transaction_amount_is_less_than_or_equal_to_balance_should_process() {
        TransactionType transactionType = TransactionType.Transfer;
        String id0 = SAVINGS_ID_0;
        String id1 = CHECKING_ID_1;
        double transferAmount = 400;

        for (int i = 0; i < 3; i++) {
            bank.withdraw(SAVINGS_ID_0, 700);
        }

        assertEquals(transferAmount, bank.getAccount(id0).getBalance());
        assertTrue(transferProcessor.handle(String.format("%s %s %s %s", transactionType, id0, id1, transferAmount)));

        assertTrue(transferAmount < bank.getAccount(id1).getBalance());
        assertTrue(transferProcessor.handle(String.format("%s %s %s %s", transactionType, id1, id0, transferAmount)));

        assertEquals(400, bank.getAccount(id0).getBalance());
        assertEquals(1000, bank.getAccount(id1).getBalance());
    }

    @Test
    protected void transfer_transaction_when_transfer_amount_is_greater_than_balance_should_transfer_amount_equal_to_balance() {
        TransactionType transactionType = TransactionType.Transfer;
        String fromID = SAVINGS_ID_0;
        String toID = SAVINGS_ID_1;
        double transferAmount = 1000;

        bank.withdraw(SAVINGS_ID_0, transferAmount);
        bank.withdraw(SAVINGS_ID_0, transferAmount);

        assertTrue(transferAmount > bank.getAccount(fromID).getBalance());
        assertTrue(transferProcessor.handle(String.format("%s %s %s %s", transactionType, fromID, toID, transferAmount)));

        assertEquals(0, bank.getAccount(fromID).getBalance());
        assertEquals(3000, bank.getAccount(toID).getBalance());
    }
}
