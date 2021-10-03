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
    protected void initialize_receipt_should_be_empty() {
        initializeReceipt();

        List<String> output = receipt.output();
        assertTrue(output.isEmpty());
    }

    @Test
    protected void output_when_state_change_should_update() {
        add_valid_create_transaction_should_be_possible();
        add_valid_deposit_transaction_should_be_possible();
    }

    @Test
    protected void add_valid_create_transaction_should_be_possible() {
        List<String> output = receipt.output();
        int i = 0;
        assertEquals(outputValid(bank, CHECKING_ID), output.get(i)); i++;
        assertEquals(outputValid(bank, ""), output.get(i)); i++;

        assertEquals(outputValid(bank, SAVINGS_ID), output.get(i)); i++;
        assertEquals(outputValid(bank, ""), output.get(i)); i++;

        assertEquals(outputValid(bank, CD_ID), output.get(i)); i++;
        assertEquals(outputValid(bank, ""), output.get(i)); i++;

        assertEquals(i, output.size());
    }

    @Test
    protected void add_valid_deposit_transaction_should_be_possible() {
        TransactionType transactionType = TransactionType.Deposit;
        String id = SAVINGS_ID;
        double depositAmount = bank.getAccount(id).getMaxDepositAmount();
        String transaction = String.format("%s %s %s", transactionType, id, depositAmount);

        receipt.addTransaction(transaction);

        List<String> output = receipt.output();
        int i = 0;
        assertEquals(outputValid(bank, CHECKING_ID), output.get(i)); i++;
        assertEquals(outputValid(bank, ""), output.get(i)); i++;

        assertEquals(outputValid(bank, SAVINGS_ID), output.get(i)); i++;
        assertEquals(outputValid(bank, transaction), output.get(i)); i++;
        assertEquals(outputValid(bank, ""), output.get(i)); i++;

        assertEquals(outputValid(bank, CD_ID), output.get(i)); i++;
        assertEquals(outputValid(bank, ""), output.get(i)); i++;

        assertEquals(i, output.size());
    }

    @Test
    protected void add_valid_withdraw_transaction_should_be_possible() {
        String id = CHECKING_ID;
        double depositAmount = bank.getAccount(id).getMaxDepositAmount();
        double withdrawAmount = bank.getAccount(id).getMaxWithdrawAmount();
        String transaction0 = String.format("%s %s %s", TransactionType.Deposit, id, depositAmount);
        String transaction1 = String.format("%s %s %s", TransactionType.Withdraw, id, withdrawAmount);

        receipt.addTransaction(transaction0);
        receipt.addTransaction(transaction1);

        List<String> output = receipt.output();
        int i = 0;
        assertEquals(outputValid(bank, CHECKING_ID), output.get(i)); i++;
        assertEquals(outputValid(bank, transaction0), output.get(i)); i++;
        assertEquals(outputValid(bank, transaction1), output.get(i)); i++;
        assertEquals(outputValid(bank, ""), output.get(i)); i++;

        assertEquals(outputValid(bank, SAVINGS_ID), output.get(i)); i++;
        assertEquals(outputValid(bank, ""), output.get(i)); i++;

        assertEquals(outputValid(bank, CD_ID), output.get(i)); i++;
        assertEquals(outputValid(bank, ""), output.get(i)); i++;

        assertEquals(i, output.size());
    }

    @Test
    protected void add_valid_transfer_transaction_should_be_possible() {
        String fromID = CHECKING_ID;
        String toID = SAVINGS_ID;
        double depositAmount = bank.getAccount(fromID).getMaxDepositAmount();
        double transferAmount = min(bank.getAccount(fromID).getMaxWithdrawAmount(), bank.getAccount(toID).getMaxDepositAmount());
        String transaction0 = String.format("%s %s %s", TransactionType.Deposit, fromID, depositAmount);
        String transaction1 = String.format("%s %s %s %s", TransactionType.Transfer, fromID, toID, transferAmount);

        receipt.addTransaction(transaction0);
        receipt.addTransaction(transaction1);

        List<String> output = receipt.output();
        int i = 0;
        assertEquals(outputValid(bank, CHECKING_ID), output.get(i)); i++;
        assertEquals(outputValid(bank, transaction0), output.get(i)); i++;
        assertEquals(outputValid(bank, transaction1), output.get(i)); i++;
        assertEquals(outputValid(bank, ""), output.get(i)); i++;

        assertEquals(outputValid(bank, SAVINGS_ID), output.get(i)); i++;
        assertEquals(outputValid(bank, transaction1), output.get(i)); i++;
        assertEquals(outputValid(bank, ""), output.get(i)); i++;

        assertEquals(outputValid(bank, CD_ID), output.get(i)); i++;
        assertEquals(outputValid(bank, ""), output.get(i)); i++;

        assertEquals(i, output.size());
    }

    @Test
    protected void add_valid_pass_time_transaction_should_be_possible() {
        TransactionType transactionType = TransactionType.PassTime;
        String transaction = String.format("%s %s", transactionType, MONTHS);

        receipt.addTransaction(transaction);

        List<String> output = receipt.output();
        int i = 0;
        assertEquals(outputValid(bank, CHECKING_ID), output.get(i)); i++;
        assertEquals(outputValid(bank, ""), output.get(i)); i++;

        assertEquals(outputValid(bank, SAVINGS_ID), output.get(i)); i++;
        assertEquals(outputValid(bank, ""), output.get(i)); i++;

        assertEquals(outputValid(bank, CD_ID), output.get(i)); i++;
        assertEquals(outputValid(bank, ""), output.get(i)); i++;

        assertEquals(i, output.size());
    }

    public static String outputValid(Bank bank, String string) {
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
    protected void add_invalid_transaction_should_be_possible() {
        initializeReceipt();

        TransactionType transactionType = TransactionType.Create;
        AccountType accountType = AccountType.CD;
        String id = "the power of friendship";
        String transaction = String.format("%s %s %s %s %s", transactionType, accountType, id, apr, initialCDBalance);

        receipt.addTransaction(transaction);

        List<String> output = receipt.output();
        int i = 0;
        assertEquals(outputInvalid(transaction), output.get(i)); i++;

        assertEquals(i, output.size());
    }

    public static String outputInvalid(String transaction) {
        return "[invalid] " + transaction;
    }
}
