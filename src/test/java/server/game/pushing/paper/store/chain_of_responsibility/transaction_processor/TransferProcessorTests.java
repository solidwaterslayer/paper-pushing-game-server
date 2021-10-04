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
import static server.game.pushing.paper.store.bank.BankTests.timeTravel;

public class TransferProcessorTests {
    private Bank bank;
    private ChainOfResponsibility processor;

    private final int MONTHS = getMonthsPerYear();
    private double minBalanceFee;
    private TransactionType transactionType;
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
        processor = new TransferProcessor(bank);

        minBalanceFee = bank.getMinBalanceFee();
        transactionType = processor.getTransactionType();
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
    protected void transfer_processor_when_transaction_can_not_process_should_pass_transaction_down_the_chain_of_responsibility() {
        processor.setNext(new TimeTravelProcessor(bank));

        assertTrue(processor.handle(String.format("%s %s", TransactionType.TimeTravel, MONTHS)));
        assertEquals(timeTravel(minBalanceFee, MONTHS, checkingDepositAmount), bank.getAccount(CHECKING_ID_0).getBalance());
        assertEquals(timeTravel(minBalanceFee, MONTHS, checkingDepositAmount), bank.getAccount(CHECKING_ID_1).getBalance());
        assertEquals(timeTravel(minBalanceFee, MONTHS, savingsDepositAmount), bank.getAccount(SAVINGS_ID_0).getBalance());
        assertEquals(timeTravel(minBalanceFee, MONTHS, savingsDepositAmount), bank.getAccount(SAVINGS_ID_1).getBalance());
        assertEquals(timeTravel(minBalanceFee, MONTHS, initialCDBalance), bank.getAccount(CD_ID).getBalance());
        assertFalse(processor.handle(String.format("%s %s %s %s %s", TransactionType.Create, AccountType.CD, "73842793", apr, initialCDBalance)));
    }

    @Test
    protected void transfer_from_checking_to_checking_transaction_should_process() {
        String payingID = CHECKING_ID_0;
        String receivingID = CHECKING_ID_1;
        double transferAmount = min(bank.getAccount(payingID).getMaxWithdrawAmount(), bank.getAccount(receivingID).getMaxDepositAmount());

        assertTrue(processor.handle(String.format("%s %s %s %s", transactionType, payingID, receivingID, transferAmount)));
        assertEquals(checkingDepositAmount - transferAmount, bank.getAccount(payingID).getBalance());
        assertEquals(checkingDepositAmount + transferAmount, bank.getAccount(receivingID).getBalance());
    }

    @Test
    protected void transfer_from_checking_to_savings_transaction_should_process() {
        String payingID = CHECKING_ID_0;
        String receivingID = SAVINGS_ID_0;
        double transferAmount = min(bank.getAccount(payingID).getMaxWithdrawAmount(), bank.getAccount(receivingID).getMaxDepositAmount());

        assertTrue(processor.handle(String.format("%s %s %s %s", transactionType, payingID, receivingID, transferAmount)));
        assertEquals(checkingDepositAmount - transferAmount, bank.getAccount(payingID).getBalance());
        assertEquals(savingsDepositAmount + transferAmount, bank.getAccount(receivingID).getBalance());
    }

    @Test
    protected void transfer_from_savings_to_checking_transaction_should_process() {
        String payingID = SAVINGS_ID_1;
        String receivingID = CHECKING_ID_0;
        double transferAmount = min(bank.getAccount(payingID).getMaxWithdrawAmount(), bank.getAccount(receivingID).getMaxDepositAmount());

        assertTrue(processor.handle(String.format("%s %s %s %s", transactionType, payingID, receivingID, transferAmount)));
        assertEquals(savingsDepositAmount - transferAmount, bank.getAccount(payingID).getBalance());
        assertEquals(checkingDepositAmount + transferAmount, bank.getAccount(receivingID).getBalance());
    }

    @Test
    protected void transfer_from_savings_to_savings_transaction_should_process() {
        String payingID = SAVINGS_ID_1;
        String receivingID = SAVINGS_ID_0;
        double transferAmount = min(bank.getAccount(payingID).getMaxWithdrawAmount(), bank.getAccount(receivingID).getMaxDepositAmount());

        assertTrue(processor.handle(String.format("%s %s %s %s", transactionType, payingID, receivingID, transferAmount)));
        assertEquals(savingsDepositAmount - transferAmount, bank.getAccount(payingID).getBalance());
        assertEquals(savingsDepositAmount + transferAmount, bank.getAccount(receivingID).getBalance());
    }

    @Test
    protected void transfer_from_cd_to_savings_transaction_should_process() {
        String payingID = CD_ID;
        String receivingID = SAVINGS_ID_1;
        double transferAmount = min(bank.getAccount(payingID).getMaxWithdrawAmount(), bank.getAccount(receivingID).getMaxDepositAmount());

        bank.timeTravel(MONTHS);

        assertTrue(processor.handle(String.format("%s %s %s %s", transactionType, payingID, receivingID, transferAmount)));
        assertEquals(0, bank.getAccount(payingID).getBalance());
        assertEquals(
                timeTravel(minBalanceFee, MONTHS, savingsDepositAmount)
                        + timeTravel(minBalanceFee, MONTHS, initialCDBalance)
                , bank.getAccount(receivingID).getBalance()
        );
    }

    @Test
    protected void transaction_when_transaction_amount_is_less_than_or_equal_to_balance_should_process() {
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
    protected void transaction_when_transfer_amount_is_greater_than_balance_should_transfer_amount_equal_to_balance() {
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
    protected void transaction_should_be_case_insensitive() {
        String payingID = CHECKING_ID_1;
        String receivingID = CHECKING_ID_0;
        double transferAmount = min(bank.getAccount(payingID).getMaxWithdrawAmount(), bank.getAccount(receivingID).getMaxDepositAmount());

        assertTrue(processor.handle(String.format("traNSFer %s %s %s", payingID, receivingID, transferAmount)));
    }

    @Test
    protected void transaction_should_be_possible_with_useless_additional_arguments() {
        String payingID = CD_ID;
        String receivingID = SAVINGS_ID_1;
        double transferAmount = min(bank.getAccount(payingID).getMaxWithdrawAmount(), bank.getAccount(receivingID).getMaxDepositAmount());

        bank.timeTravel(12);

        assertTrue(processor.handle(String.format("%s %s %s %s %s", transactionType, payingID, receivingID, transferAmount, "nuke")));
        assertTrue(processor.handle(String.format("%s %s %s %s  %s  %s %s  %s   %s", transactionType, payingID, receivingID, transferAmount, "DsDifJ", "paSJiOf", "ps3f&jf", "sp@&HR*&HDSoa", "psd)(Jo")));
    }
}
