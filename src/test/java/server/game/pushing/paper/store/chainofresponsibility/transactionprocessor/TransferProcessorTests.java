package server.game.pushing.paper.store.chainofresponsibility.transactionprocessor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.game.pushing.paper.store.bank.Bank;
import server.game.pushing.paper.store.bank.account.AccountType;
import server.game.pushing.paper.store.chainofresponsibility.ChainOfResponsibility;
import server.game.pushing.paper.store.chainofresponsibility.TransactionType;

import java.util.Arrays;

import static java.lang.Math.min;
import static org.junit.jupiter.api.Assertions.*;
import static server.game.pushing.paper.store.bank.Bank.getMonthsPerYear;
import static server.game.pushing.paper.store.bank.BankTests.passTime;

public class TransferProcessorTests {
    private Bank bank;
    private TransferProcessor transferProcessor;

    private final String CHECKING_ID_0 = "98830842";
    private final String CHECKING_ID_1 = "09309843";
    private final String SAVINGS_ID_0 = "90328934";
    private final String SAVINGS_ID_1 = "11117823";
    private final String CD_ID = "08429834";
    private double apr;
    private double initialCDBalance;
    private double checkingDepositAmount;
    private double savingsDepositAmount;

    @BeforeEach
    protected void setUp() {
        bank = new Bank();
        transferProcessor = new TransferProcessor(bank);

        apr = bank.getMaxAPR();
        initialCDBalance = bank.getMinInitialCDBalance();
        bank.createChecking(CHECKING_ID_0, apr);
        bank.createChecking(CHECKING_ID_1, apr);
        bank.createSavings(SAVINGS_ID_0, apr);
        bank.createSavings(SAVINGS_ID_1, apr);
        bank.createCD(CD_ID, apr, initialCDBalance);
        checkingDepositAmount = bank.getAccount(CHECKING_ID_1).getMaxDepositAmount();
        savingsDepositAmount = bank.getAccount(SAVINGS_ID_1).getMaxDepositAmount();

        bank.deposit(CHECKING_ID_0, checkingDepositAmount);
        bank.deposit(CHECKING_ID_1, checkingDepositAmount);
        bank.deposit(SAVINGS_ID_0, savingsDepositAmount);
        bank.deposit(SAVINGS_ID_1, savingsDepositAmount);
    }

    @Test
    protected void transfer_processor_when_transaction_can_not_process_should_pass_transaction_up_the_chain_of_responsibility() {
        transferProcessor = (TransferProcessor) ChainOfResponsibility.getInstance(Arrays.asList(transferProcessor, new PassTimeProcessor(bank), null));

        double minBalanceFee = bank.getMinBalanceFee();
        int months = getMonthsPerYear();

        assertTrue(transferProcessor.handle(String.format("%s %s", TransactionType.PassTime, months)));
        assertEquals(passTime(minBalanceFee, months, AccountType.Checking, apr, checkingDepositAmount), bank.getAccount(CHECKING_ID_0).getBalance());
        assertEquals(passTime(minBalanceFee, months, AccountType.Checking, apr, checkingDepositAmount), bank.getAccount(CHECKING_ID_1).getBalance());
        assertEquals(passTime(minBalanceFee, months, AccountType.Savings, apr, savingsDepositAmount), bank.getAccount(SAVINGS_ID_0).getBalance());
        assertEquals(passTime(minBalanceFee, months, AccountType.Savings, apr, savingsDepositAmount), bank.getAccount(SAVINGS_ID_1).getBalance());
        assertEquals(passTime(minBalanceFee, months, AccountType.CD, apr, initialCDBalance), bank.getAccount(CD_ID).getBalance());
        assertFalse(transferProcessor.handle(String.format("%s %s %s %s %s", TransactionType.Create, AccountType.CD, "73842793", apr, initialCDBalance)));
    }

    @Test
    protected void transfer_from_checking_to_checking_transaction_should_process() {
        TransactionType transactionType = TransactionType.Transfer;
        String fromID = CHECKING_ID_0;
        String toID = CHECKING_ID_1;
        double transferAmount = min(bank.getAccount(fromID).getMaxWithdrawAmount(), bank.getAccount(toID).getMaxDepositAmount());

        assertTrue(transferProcessor.handle(String.format("%s %s %s %s", transactionType, fromID, toID, transferAmount)));
        assertEquals(checkingDepositAmount - transferAmount, bank.getAccount(fromID).getBalance());
        assertEquals(checkingDepositAmount + transferAmount, bank.getAccount(toID).getBalance());
    }

    @Test
    protected void transfer_from_checking_to_savings_transaction_should_process() {
        TransactionType transactionType = TransactionType.Transfer;
        String fromID = CHECKING_ID_0;
        String toID = SAVINGS_ID_0;
        double transferAmount = min(bank.getAccount(fromID).getMaxWithdrawAmount(), bank.getAccount(toID).getMaxDepositAmount());

        assertTrue(transferProcessor.handle(String.format("%s %s %s %s", transactionType, fromID, toID, transferAmount)));
        assertEquals(checkingDepositAmount - transferAmount, bank.getAccount(fromID).getBalance());
        assertEquals(savingsDepositAmount + transferAmount, bank.getAccount(toID).getBalance());
    }

    @Test
    protected void transfer_from_savings_to_checking_transaction_should_process() {
        TransactionType transactionType = TransactionType.Transfer;
        String fromID = SAVINGS_ID_1;
        String toID = CHECKING_ID_0;
        double transferAmount = min(bank.getAccount(fromID).getMaxWithdrawAmount(), bank.getAccount(toID).getMaxDepositAmount());

        assertTrue(transferProcessor.handle(String.format("%s %s %s %s", transactionType, fromID, toID, transferAmount)));
        assertEquals(savingsDepositAmount - transferAmount, bank.getAccount(fromID).getBalance());
        assertEquals(checkingDepositAmount + transferAmount, bank.getAccount(toID).getBalance());
    }

    @Test
    protected void transfer_from_savings_to_savings_transaction_should_process() {
        TransactionType transactionType = TransactionType.Transfer;
        String fromID = SAVINGS_ID_1;
        String toID = SAVINGS_ID_0;
        double transferAmount = min(bank.getAccount(fromID).getMaxWithdrawAmount(), bank.getAccount(toID).getMaxDepositAmount());

        assertTrue(transferProcessor.handle(String.format("%s %s %s %s", transactionType, fromID, toID, transferAmount)));
        assertEquals(savingsDepositAmount - transferAmount, bank.getAccount(fromID).getBalance());
        assertEquals(savingsDepositAmount + transferAmount, bank.getAccount(toID).getBalance());
    }

    @Test
    protected void transfer_from_cd_to_savings_transaction_should_process() {
        int months = getMonthsPerYear();
        double minBalanceFee = bank.getMinBalanceFee();
        TransactionType transactionType = TransactionType.Transfer;
        String fromID = CD_ID;
        String toID = SAVINGS_ID_1;
        double transferAmount = min(bank.getAccount(fromID).getMaxWithdrawAmount(), bank.getAccount(toID).getMaxDepositAmount());

        bank.passTime(months);

        assertTrue(transferProcessor.handle(String.format("%s %s %s %s", transactionType, fromID, toID, transferAmount)));
        assertEquals(0, bank.getAccount(fromID).getBalance());
        assertEquals(
                passTime(minBalanceFee, months, AccountType.Savings, apr, savingsDepositAmount)
                        + passTime(minBalanceFee, months, AccountType.CD, apr, initialCDBalance)
                , bank.getAccount(toID).getBalance()
        );
    }

    @Test
    protected void transaction_when_transaction_amount_is_less_than_or_equal_to_balance_should_process() {
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
    protected void transaction_when_transfer_amount_is_greater_than_balance_should_transfer_amount_equal_to_balance() {
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

    @Test
    protected void transaction_should_be_case_insensitive() {
        String fromID = CHECKING_ID_1;
        String toID = CHECKING_ID_0;
        double transferAmount = min(bank.getAccount(fromID).getMaxWithdrawAmount(), bank.getAccount(toID).getMaxDepositAmount());

        assertTrue(transferProcessor.handle(String.format("traNSFer %s %s %s", fromID, toID, transferAmount)));
    }

    @Test
    protected void transaction_should_be_possible_with_useless_additional_arguments() {
        TransactionType transactionType = TransactionType.Transfer;
        String fromID = CD_ID;
        String toID = SAVINGS_ID_1;
        double transferAmount = min(bank.getAccount(fromID).getMaxWithdrawAmount(), bank.getAccount(toID).getMaxDepositAmount());

        bank.passTime(12);

        assertTrue(transferProcessor.handle(String.format("%s %s %s %s %s", transactionType, fromID, toID, transferAmount, "nuke")));
        assertTrue(transferProcessor.handle(String.format("%s %s %s %s  %s  %s %s  %s   %s", transactionType, fromID, toID, transferAmount, "DsDifJ", "paSJiOf", "ps3f&jf", "sp@&HR*&HDSoa", "psd)(Jo")));
    }
}
