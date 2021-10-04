package server.game.pushing.paper.store;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.game.pushing.paper.store.bank.Bank;
import server.game.pushing.paper.store.bank.account.Account;
import server.game.pushing.paper.store.bank.account.AccountType;
import server.game.pushing.paper.store.chain_of_responsibility.TransactionType;

import java.util.List;

import static java.lang.Math.min;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static server.game.pushing.paper.store.bank.Bank.getMonthsPerYear;
import static server.game.pushing.paper.store.chain_of_responsibility.ChainOfResponsibility.parseDouble;

public class ReceiptTests {
    private Bank bank;
    private Receipt receipt;

    private final int MONTHS = getMonthsPerYear();
    private final String CHECKING_ID = "00000000";
    private final String SAVINGS_ID = "00000001";
    private final String CD_ID = "00000010";
    private double apr;
    private double initialCDBalance;

    @BeforeEach
    protected void setUp() {
        initializeReceipt();

        TransactionType transactionType = TransactionType.Create;
        apr = bank.getMaxAPR();
        initialCDBalance = bank.getMinInitialCDBalance();

        receipt.addTransaction(String.format("%s %s %s %s", transactionType, AccountType.CHECKING, CHECKING_ID, apr));
        receipt.addTransaction(String.format("%s %s %s %s", transactionType, AccountType.SAVINGS, SAVINGS_ID, apr));
        receipt.addTransaction(String.format("%s %s %s %s %s", transactionType, AccountType.CD, CD_ID, apr, initialCDBalance));
    }

    private void initializeReceipt() {
        bank = new Bank();
        receipt = new Receipt(bank);
    }

    @Test
    protected void receipts_should_start_with_0_transactions() {
        initializeReceipt();

        List<String> output = receipt.output();
        assertTrue(output.isEmpty());
    }

    @Test
    protected void receipts_should_update_when_their_state_changes() {
        receipts_can_add_valid_create_transactions();
        receipts_can_add_valid_deposit_transactions();
    }

    @Test
    protected void receipts_can_add_valid_create_transactions() {
        List<String> output = receipt.output();
        int i = 0;
        assertEquals(output(bank, CHECKING_ID), output.get(i)); i++;
        assertEquals(output(bank, ""), output.get(i)); i++;

        assertEquals(output(bank, SAVINGS_ID), output.get(i)); i++;
        assertEquals(output(bank, ""), output.get(i)); i++;

        assertEquals(output(bank, CD_ID), output.get(i)); i++;
        assertEquals(output(bank, ""), output.get(i)); i++;

        assertEquals(i, output.size());
    }

    @Test
    protected void receipts_can_add_valid_deposit_transactions() {
        TransactionType transactionType = TransactionType.Deposit;
        String id = SAVINGS_ID;
        double depositAmount = bank.getAccount(id).getMaxDepositAmount();
        String transaction = String.format("%s %s %s", transactionType, id, depositAmount);

        receipt.addTransaction(transaction);

        List<String> output = receipt.output();
        int i = 0;
        assertEquals(output(bank, CHECKING_ID), output.get(i)); i++;
        assertEquals(output(bank, ""), output.get(i)); i++;

        assertEquals(output(bank, SAVINGS_ID), output.get(i)); i++;
        assertEquals(output(bank, transaction), output.get(i)); i++;
        assertEquals(output(bank, ""), output.get(i)); i++;

        assertEquals(output(bank, CD_ID), output.get(i)); i++;
        assertEquals(output(bank, ""), output.get(i)); i++;

        assertEquals(i, output.size());
    }

    @Test
    protected void receipts_can_add_valid_withdraw_transactions() {
        String id = CHECKING_ID;
        double depositAmount = bank.getAccount(id).getMaxDepositAmount();
        double withdrawAmount = bank.getAccount(id).getMaxWithdrawAmount();
        String transaction0 = String.format("%s %s %s", TransactionType.Deposit, id, depositAmount);
        String transaction1 = String.format("%s %s %s", TransactionType.Withdraw, id, withdrawAmount);

        receipt.addTransaction(transaction0);
        receipt.addTransaction(transaction1);

        List<String> output = receipt.output();
        int i = 0;
        assertEquals(output(bank, CHECKING_ID), output.get(i)); i++;
        assertEquals(output(bank, transaction0), output.get(i)); i++;
        assertEquals(output(bank, transaction1), output.get(i)); i++;
        assertEquals(output(bank, ""), output.get(i)); i++;

        assertEquals(output(bank, SAVINGS_ID), output.get(i)); i++;
        assertEquals(output(bank, ""), output.get(i)); i++;

        assertEquals(output(bank, CD_ID), output.get(i)); i++;
        assertEquals(output(bank, ""), output.get(i)); i++;

        assertEquals(i, output.size());
    }

    @Test
    protected void receipts_can_add_valid_transfer_transactions() {
        String payingID = CHECKING_ID;
        String receivingID = SAVINGS_ID;
        double depositAmount = bank.getAccount(payingID).getMaxDepositAmount();
        double transferAmount = min(bank.getAccount(payingID).getMaxWithdrawAmount(), bank.getAccount(receivingID).getMaxDepositAmount());
        String transaction0 = String.format("%s %s %s", TransactionType.Deposit, payingID, depositAmount);
        String transaction1 = String.format("%s %s %s %s", TransactionType.Transfer, payingID, receivingID, transferAmount);

        receipt.addTransaction(transaction0);
        receipt.addTransaction(transaction1);

        List<String> output = receipt.output();
        int i = 0;
        assertEquals(output(bank, CHECKING_ID), output.get(i)); i++;
        assertEquals(output(bank, transaction0), output.get(i)); i++;
        assertEquals(output(bank, transaction1), output.get(i)); i++;
        assertEquals(output(bank, ""), output.get(i)); i++;

        assertEquals(output(bank, SAVINGS_ID), output.get(i)); i++;
        assertEquals(output(bank, transaction1), output.get(i)); i++;
        assertEquals(output(bank, ""), output.get(i)); i++;

        assertEquals(output(bank, CD_ID), output.get(i)); i++;
        assertEquals(output(bank, ""), output.get(i)); i++;

        assertEquals(i, output.size());
    }

    @Test
    protected void receipts_can_add_valid_time_travel_transactions() {
        TransactionType transactionType = TransactionType.TimeTravel;
        String transaction = String.format("%s %s", transactionType, MONTHS);

        receipt.addTransaction(transaction);

        List<String> output = receipt.output();
        int i = 0;
        assertEquals(output(bank, CHECKING_ID), output.get(i)); i++;
        assertEquals(output(bank, ""), output.get(i)); i++;

        assertEquals(output(bank, SAVINGS_ID), output.get(i)); i++;
        assertEquals(output(bank, ""), output.get(i)); i++;

        assertEquals(output(bank, CD_ID), output.get(i)); i++;
        assertEquals(output(bank, ""), output.get(i)); i++;

        assertEquals(i, output.size());
    }

    public static String output(Bank bank, String string) {
        if (bank.containsAccount(string)) {
            Account account = bank.getAccount(string);
            return String.format("%s %s %.2f %.2f", account.getAccountType(), account.getID(), account.getAPR(), account.getBalance()).toLowerCase();
        }
        String[] transactionArguments = string.toLowerCase().split(" ");
        if (transactionArguments[0].equalsIgnoreCase(TransactionType.Deposit.name()) || transactionArguments[0].equalsIgnoreCase(TransactionType.Withdraw.name())) {
            return String.format("%s %s %.2f", transactionArguments[0], transactionArguments[1], parseDouble(transactionArguments[2]));
        }
        if (transactionArguments[0].equalsIgnoreCase(TransactionType.Transfer.name())) {
            return String.format("%s %s %s %.2f", transactionArguments[0], transactionArguments[1], transactionArguments[2], parseDouble(transactionArguments[3]));
        }

        return string;
    }

    @Test
    protected void receipts_can_add_invalid_transactions() {
        initializeReceipt();

        TransactionType transactionType = TransactionType.Create;
        AccountType accountType = AccountType.CD;
        String id = "the power of friendship";
        String transaction = String.format("%s %s %s %s %s", transactionType, accountType, id, apr, initialCDBalance);

        receipt.addTransaction(transaction);

        List<String> output = receipt.output();
        int i = 0;
        assertEquals(output(transaction), output.get(i)); i++;

        assertEquals(i, output.size());
    }

    public static String output(String transaction) {
        return "[invalid] " + transaction;
    }
}
