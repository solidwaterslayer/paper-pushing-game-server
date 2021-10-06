package server.game.pushing.paper.store;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.game.pushing.paper.store.bank.Bank;
import server.game.pushing.paper.store.bank.account.AccountType;
import server.game.pushing.paper.store.chain_of_responsibility.TransactionType;

import java.util.List;

import static java.lang.Math.min;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static server.game.pushing.paper.store.bank.Bank.getMonthsPerYear;

public class StoreTests {
    private Store store;
    private Bank bank;

    private double minBalanceFee;
    private final int MONTHS = getMonthsPerYear();
    private final String CHECKING_ID_0 = "00000000";
    private final String CHECKING_ID_1 = "10000000";
    private final String SAVINGS_ID_0 = "00000001";
    private final String SAVINGS_ID_1 = "10000001";
    private final String CD_ID = "10000010";
    private double initialCDBalance;
    private double checkingDepositAmount;
    private double savingsDepositAmount;
    private double checkingWithdrawAmount;
    private double savingsWithdrawAmount;
    private double cdWithdrawAmount;

    @BeforeEach
    protected void setUp() {
        initializeStore();

        TransactionType transactionType = TransactionType.Create;
        minBalanceFee = bank.getMinBalanceFee();
        bank.createChecking(CHECKING_ID_1);
        bank.createSavings(SAVINGS_ID_1);
        bank.createCD(CD_ID, initialCDBalance);
        initialCDBalance = bank.getMinInitialCDBalance();
        checkingDepositAmount = bank.getAccount(CHECKING_ID_1).getMaxDepositAmount();
        savingsDepositAmount = bank.getAccount(SAVINGS_ID_1).getMaxDepositAmount();
        checkingWithdrawAmount = bank.getAccount(CHECKING_ID_1).getMaxWithdrawAmount();
        savingsWithdrawAmount = bank.getAccount(SAVINGS_ID_1).getMaxWithdrawAmount();
        cdWithdrawAmount = bank.getAccount(CD_ID).getMaxWithdrawAmount();

        initializeStore();
        store.getOrder().add(0, String.format("%s %s %s", transactionType, AccountType.CHECKING, CHECKING_ID_1));
        store.getOrder().add(1, String.format("%s %s %s", transactionType, AccountType.CHECKING, CHECKING_ID_0));
        store.getOrder().add(2, String.format("%s %s %s", transactionType, AccountType.SAVINGS, SAVINGS_ID_1));
        store.getOrder().add(3, String.format("%s %s %s", transactionType, AccountType.SAVINGS, SAVINGS_ID_0));
        store.getOrder().add(4, String.format("%s %s %s %s", transactionType, AccountType.CD, CD_ID, initialCDBalance));
    }

    private void initializeStore() {
        store = new Store();
        bank = store.getBank();
    }

    @Test
    protected void stores_should_start_with_an_empty_receipt() {
        initializeStore();

        List<String> receipt = store.getReceipt();
        assertTrue(receipt.isEmpty());
    }

    @Test
    protected void stores_should_update_when_their_state_changes() {
        valid_create_transactions_should_output_account_type_id_and_balance();
        valid_deposit_transactions_should_output_themselves();
    }

    @Test
    protected void valid_create_transactions_should_output_account_type_id_and_balance() {
        List<String> receipt = store.getReceipt();
        int i = 0;
        assertEquals(ReceiptTests.output(bank, CHECKING_ID_1), receipt.get(i)); i++;
        assertEquals(ReceiptTests.output(bank, ""), receipt.get(i)); i++;

        assertEquals(ReceiptTests.output(bank, CHECKING_ID_0), receipt.get(i)); i++;
        assertEquals(ReceiptTests.output(bank, ""), receipt.get(i)); i++;

        assertEquals(ReceiptTests.output(bank, SAVINGS_ID_1), receipt.get(i)); i++;
        assertEquals(ReceiptTests.output(bank, ""), receipt.get(i)); i++;

        assertEquals(ReceiptTests.output(bank, SAVINGS_ID_0), receipt.get(i)); i++;
        assertEquals(ReceiptTests.output(bank, ""), receipt.get(i)); i++;

        assertEquals(ReceiptTests.output(bank, CD_ID), receipt.get(i)); i++;
        assertEquals(ReceiptTests.output(bank, ""), receipt.get(i)); i++;

        assertEquals(i, receipt.size());
    }

    @Test
    protected void valid_deposit_transactions_should_output_themselves() {
        TransactionType transactionType = TransactionType.Deposit;

        store.getOrder().add(5, String.format("%s %s %s", transactionType, CHECKING_ID_1, checkingDepositAmount));
        store.getOrder().add(6, String.format("%s %s %s", transactionType, SAVINGS_ID_1, savingsDepositAmount));

        List<String> receipt = store.getReceipt();
        int i = 0;
        assertEquals(ReceiptTests.output(bank, CHECKING_ID_1), receipt.get(i)); i++;
        assertEquals(ReceiptTests.output(bank, store.getOrder().get(5)), receipt.get(i)); i++;
        assertEquals(ReceiptTests.output(bank, ""), receipt.get(i)); i++;

        assertEquals(ReceiptTests.output(bank, CHECKING_ID_0), receipt.get(i)); i++;
        assertEquals(ReceiptTests.output(bank, ""), receipt.get(i)); i++;

        assertEquals(ReceiptTests.output(bank, SAVINGS_ID_1), receipt.get(i)); i++;
        assertEquals(ReceiptTests.output(bank, store.getOrder().get(6)), receipt.get(i)); i++;
        assertEquals(ReceiptTests.output(bank, ""), receipt.get(i)); i++;

        assertEquals(ReceiptTests.output(bank, SAVINGS_ID_0), receipt.get(i)); i++;
        assertEquals(ReceiptTests.output(bank, ""), receipt.get(i)); i++;

        assertEquals(ReceiptTests.output(bank, CD_ID), receipt.get(i)); i++;
        assertEquals(ReceiptTests.output(bank, ""), receipt.get(i)); i++;

        assertEquals(i, receipt.size());
    }

    @Test
    protected void valid_withdraw_transactions_should_output_themselves() {
        store.getOrder().add(5, String.format("%s %s %s", TransactionType.Deposit, CHECKING_ID_1, checkingDepositAmount));
        store.getOrder().add(6, String.format("%s %s %s", TransactionType.Deposit, SAVINGS_ID_1, savingsDepositAmount));
        store.getOrder().add(7, String.format("%s %s", TransactionType.TimeTravel, MONTHS));
        store.getOrder().add(8, String.format("%s %s %s", TransactionType.Withdraw, CHECKING_ID_1, checkingWithdrawAmount));
        store.getOrder().add(9, String.format("%s %s %s", TransactionType.Withdraw, SAVINGS_ID_1, savingsWithdrawAmount));
        store.getOrder().add(10, String.format("%s %s %s", TransactionType.Withdraw, CD_ID, cdWithdrawAmount));

        List<String> receipt = store.getReceipt();
        int i = 0;
        assertEquals(ReceiptTests.output(bank, CHECKING_ID_1), receipt.get(i)); i++;
        assertEquals(ReceiptTests.output(bank, store.getOrder().get(5)), receipt.get(i)); i++;
        assertEquals(ReceiptTests.output(bank, store.getOrder().get(8)), receipt.get(i)); i++;
        assertEquals(ReceiptTests.output(bank, ""), receipt.get(i)); i++;

        assertEquals(ReceiptTests.output(bank, CHECKING_ID_0), receipt.get(i)); i++;
        assertEquals(ReceiptTests.output(bank, ""), receipt.get(i)); i++;

        assertEquals(ReceiptTests.output(bank, SAVINGS_ID_1), receipt.get(i)); i++;
        assertEquals(ReceiptTests.output(bank, store.getOrder().get(6)), receipt.get(i)); i++;
        assertEquals(ReceiptTests.output(bank, store.getOrder().get(9)), receipt.get(i)); i++;
        assertEquals(ReceiptTests.output(bank, ""), receipt.get(i)); i++;

        assertEquals(ReceiptTests.output(bank, SAVINGS_ID_0), receipt.get(i)); i++;
        assertEquals(ReceiptTests.output(bank, ""), receipt.get(i)); i++;

        assertEquals(ReceiptTests.output(bank, CD_ID), receipt.get(i)); i++;
        assertEquals(ReceiptTests.output(bank, store.getOrder().get(10)), receipt.get(i)); i++;
        assertEquals(ReceiptTests.output(bank, ""), receipt.get(i)); i++;

        assertEquals(i, receipt.size());
    }

    @Test
    protected void valid_transfer_transactions_should_output_themselves_twice() {
        store.getOrder().add(5, String.format("%s %s %s", TransactionType.Deposit, CHECKING_ID_1, checkingDepositAmount));
        store.getOrder().add(6, String.format("%s %s %s", TransactionType.Deposit, CHECKING_ID_0, checkingDepositAmount));
        store.getOrder().add(7, String.format("%s %s %s", TransactionType.Deposit, SAVINGS_ID_1, savingsDepositAmount));
        store.getOrder().add(8, String.format("%s %s %s", TransactionType.Deposit, SAVINGS_ID_0, savingsDepositAmount));
        store.getOrder().add(9, String.format("%s %s", TransactionType.TimeTravel, MONTHS));
        store.getOrder().add(10, String.format("%s %s %s %s", TransactionType.Transfer, CHECKING_ID_1, CHECKING_ID_0, min(checkingWithdrawAmount, checkingDepositAmount)));
        store.getOrder().add(11, String.format("%s %s %s %s", TransactionType.Transfer, CHECKING_ID_1, SAVINGS_ID_1, min(checkingWithdrawAmount, savingsDepositAmount)));
        store.getOrder().add(12, String.format("%s %s %s %s", TransactionType.Transfer, SAVINGS_ID_1, CHECKING_ID_1, min(savingsWithdrawAmount, checkingDepositAmount)));
        store.getOrder().add(13, String.format("%s %s %s %s", TransactionType.Transfer, SAVINGS_ID_0, SAVINGS_ID_1, min(savingsWithdrawAmount, savingsDepositAmount)));
        store.getOrder().add(14, String.format("%s %s %s %s", TransactionType.Transfer, CD_ID, SAVINGS_ID_1, min(cdWithdrawAmount, savingsDepositAmount)));

        List<String> receipt = store.getReceipt();
        int i = 0;
        assertEquals(ReceiptTests.output(bank, CHECKING_ID_1), receipt.get(i)); i++;
        assertEquals(ReceiptTests.output(bank, store.getOrder().get(5)), receipt.get(i)); i++;
        assertEquals(ReceiptTests.output(bank, store.getOrder().get(10)), receipt.get(i)); i++;
        assertEquals(ReceiptTests.output(bank, store.getOrder().get(11)), receipt.get(i)); i++;
        assertEquals(ReceiptTests.output(bank, store.getOrder().get(12)), receipt.get(i)); i++;
        assertEquals(ReceiptTests.output(bank, ""), receipt.get(i)); i++;

        assertEquals(ReceiptTests.output(bank, CHECKING_ID_0), receipt.get(i)); i++;
        assertEquals(ReceiptTests.output(bank, store.getOrder().get(6)), receipt.get(i)); i++;
        assertEquals(ReceiptTests.output(bank, store.getOrder().get(10)), receipt.get(i)); i++;
        assertEquals(ReceiptTests.output(bank, ""), receipt.get(i)); i++;

        assertEquals(ReceiptTests.output(bank, SAVINGS_ID_1), receipt.get(i)); i++;
        assertEquals(ReceiptTests.output(bank, store.getOrder().get(7)), receipt.get(i)); i++;
        assertEquals(ReceiptTests.output(bank, store.getOrder().get(11)), receipt.get(i)); i++;
        assertEquals(ReceiptTests.output(bank, store.getOrder().get(12)), receipt.get(i)); i++;
        assertEquals(ReceiptTests.output(bank, store.getOrder().get(13)), receipt.get(i)); i++;
        assertEquals(ReceiptTests.output(bank, store.getOrder().get(14)), receipt.get(i)); i++;
        assertEquals(ReceiptTests.output(bank, ""), receipt.get(i)); i++;

        assertEquals(ReceiptTests.output(bank, SAVINGS_ID_0), receipt.get(i)); i++;
        assertEquals(ReceiptTests.output(bank, store.getOrder().get(8)), receipt.get(i)); i++;
        assertEquals(ReceiptTests.output(bank, store.getOrder().get(13)), receipt.get(i)); i++;
        assertEquals(ReceiptTests.output(bank, ""), receipt.get(i)); i++;

        assertEquals(ReceiptTests.output(bank, CD_ID), receipt.get(i)); i++;
        assertEquals(ReceiptTests.output(bank, store.getOrder().get(14)), receipt.get(i)); i++;
        assertEquals(ReceiptTests.output(bank, ""), receipt.get(i)); i++;

        assertEquals(i, receipt.size());
    }

    @Test
    protected void valid_time_travel_transactions_should_not_output() {
        store.getOrder().add(5, String.format("%s %s %s", TransactionType.Deposit, CHECKING_ID_1, checkingDepositAmount));
        store.getOrder().add(6, String.format("%s %s %s", TransactionType.Deposit, SAVINGS_ID_1, savingsDepositAmount));
        store.getOrder().add(7, String.format("%s %s", TransactionType.TimeTravel, MONTHS));

        List<String> receipt = store.getReceipt();
        int i = 0;
        assertEquals(ReceiptTests.output(bank, CHECKING_ID_1), receipt.get(i)); i++;
        assertEquals(ReceiptTests.output(bank, store.getOrder().get(5)), receipt.get(i)); i++;
        assertEquals(ReceiptTests.output(bank, ""), receipt.get(i)); i++;

        assertEquals(ReceiptTests.output(bank, CHECKING_ID_0), receipt.get(i)); i++;
        assertEquals(ReceiptTests.output(bank, ""), receipt.get(i)); i++;

        assertEquals(ReceiptTests.output(bank, SAVINGS_ID_1), receipt.get(i)); i++;
        assertEquals(ReceiptTests.output(bank, store.getOrder().get(6)), receipt.get(i)); i++;
        assertEquals(ReceiptTests.output(bank, ""), receipt.get(i)); i++;

        assertEquals(ReceiptTests.output(bank, SAVINGS_ID_0), receipt.get(i)); i++;
        assertEquals(ReceiptTests.output(bank, ""), receipt.get(i)); i++;

        assertEquals(ReceiptTests.output(bank, CD_ID), receipt.get(i)); i++;
        assertEquals(ReceiptTests.output(bank, ""), receipt.get(i)); i++;

        assertEquals(i, receipt.size());
    }

    @Test
    protected void valid_transactions_should_output_lower_case_without_additional_arguments() {
        store.getOrder().add(5, String.format("%s %s %s %s %s %s %s", "dEpOsIt", SAVINGS_ID_0, savingsDepositAmount, "the", "power", "of", "friendship"));
        store.getOrder().add(6, String.format("%s %s %s %s %s %s %s %s", "tRAnSFeR", SAVINGS_ID_0, CHECKING_ID_1, min(savingsWithdrawAmount, checkingDepositAmount), store, 1, minBalanceFee, min(437, 234)));
        store.getOrder().add(7, String.format("%s %s %s %s %s %s %s %s    %s %s %s    %s %s %s    %s %s %s %s %s %s %s %s %s %s %s %s   %s %s %s   %s %s %s", "tiMe tRaVeL", MONTHS, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, "last one"));
        store.getOrder().add(8, String.format("%s %s %s %s        ", "wiTHdRAW", SAVINGS_ID_0, savingsWithdrawAmount, 0));

        List<String> receipt = store.getReceipt();
        int i = 0;
        assertEquals(ReceiptTests.output(bank, CHECKING_ID_1), receipt.get(i)); i++;
        assertEquals(ReceiptTests.output(bank, store.getOrder().get(6)), receipt.get(i)); i++;
        assertEquals(ReceiptTests.output(bank, ""), receipt.get(i)); i++;

        assertEquals(ReceiptTests.output(bank, CHECKING_ID_0), receipt.get(i)); i++;
        assertEquals(ReceiptTests.output(bank, ""), receipt.get(i)); i++;

        assertEquals(ReceiptTests.output(bank, SAVINGS_ID_1), receipt.get(i)); i++;
        assertEquals(ReceiptTests.output(bank, ""), receipt.get(i)); i++;

        assertEquals(ReceiptTests.output(bank, SAVINGS_ID_0), receipt.get(i)); i++;
        assertEquals(ReceiptTests.output(bank, store.getOrder().get(5)), receipt.get(i)); i++;
        assertEquals(ReceiptTests.output(bank, store.getOrder().get(6)), receipt.get(i)); i++;
        assertEquals(ReceiptTests.output(bank, store.getOrder().get(8)), receipt.get(i)); i++;
        assertEquals(ReceiptTests.output(bank, ""), receipt.get(i)); i++;

        assertEquals(ReceiptTests.output(bank, CD_ID), receipt.get(i)); i++;
        assertEquals(ReceiptTests.output(bank, ""), receipt.get(i)); i++;

        assertEquals(i, receipt.size());
    }

    @Test
    protected void invalid_create_transactions_should_output_themselves_after_an_invalid_tag() {
        initializeStore();
        TransactionType transactionType = TransactionType.Create;

        store.getOrder().add(0, String.format("%s %s %s", "", AccountType.CHECKING, CHECKING_ID_1));
        store.getOrder().add(1, String.format("%s %s %s", "transactionType", AccountType.SAVINGS, SAVINGS_ID_1));
        store.getOrder().add(2, String.format("%s %s %s %s", transactionType, "", CD_ID, initialCDBalance));
        store.getOrder().add(3, String.format("%s %s %s", transactionType, "AccountType.Savings", CHECKING_ID_1));
        store.getOrder().add(4, String.format("%s %s %s", transactionType, AccountType.SAVINGS, ""));
        store.getOrder().add(5, String.format("%s %s %s %s", transactionType, AccountType.CD, "SAVINGS_ID_0", initialCDBalance));
        store.getOrder().add(6, String.format("%s %s %s %s", transactionType, AccountType.CD, CD_ID, ""));
        store.getOrder().add(7, String.format("%s %s %s %s", transactionType, AccountType.CD, CD_ID, "initialCDBalance"));
        store.getOrder().add(8, String.format("%s %s %s %s", transactionType, AccountType.CD, CD_ID, -178234));

        List<String> receipt = store.getReceipt();
        int i;
        for (i = 0; i < 9; i++) {
            assertEquals(ReceiptTests.output(store.getOrder().get(i)), receipt.get(i));
        }

        assertEquals(i, receipt.size());
    }

    @Test
    protected void invalid_deposit_transactions_should_output_themselves_after_an_invalid_tag() {
        TransactionType transactionType = TransactionType.Deposit;

        store.getOrder().add(5, String.format("%s %s %s", "", CHECKING_ID_1, checkingDepositAmount));
        store.getOrder().add(6, String.format("%s %s %s", "TransactionType.Deposit", SAVINGS_ID_1, savingsDepositAmount));
        store.getOrder().add(7, String.format("%s %s %s", transactionType, "", checkingDepositAmount));
        store.getOrder().add(8, String.format("%s %s %s", transactionType, "SAVINGS_ID_1", savingsDepositAmount));
        store.getOrder().add(9, String.format("%s %s %s", transactionType, CD_ID, checkingDepositAmount));
        store.getOrder().add(10, String.format("%s %s %s", transactionType, SAVINGS_ID_1, ""));
        store.getOrder().add(11, String.format("%s %s %s", transactionType, CHECKING_ID_1, "savingsDepositAmount"));
        store.getOrder().add(12, String.format("%s %s %s", transactionType, SAVINGS_ID_1, 23487984));

        List<String> receipt = store.getReceipt();
        int i = 0;
        assertEquals(ReceiptTests.output(bank, CHECKING_ID_1), receipt.get(i)); i++;
        assertEquals(ReceiptTests.output(bank, ""), receipt.get(i)); i++;

        assertEquals(ReceiptTests.output(bank, CHECKING_ID_0), receipt.get(i)); i++;
        assertEquals(ReceiptTests.output(bank, ""), receipt.get(i)); i++;

        assertEquals(ReceiptTests.output(bank, SAVINGS_ID_1), receipt.get(i)); i++;
        assertEquals(ReceiptTests.output(bank, ""), receipt.get(i)); i++;

        assertEquals(ReceiptTests.output(bank, SAVINGS_ID_0), receipt.get(i)); i++;
        assertEquals(ReceiptTests.output(bank, ""), receipt.get(i)); i++;

        assertEquals(ReceiptTests.output(bank, CD_ID), receipt.get(i)); i++;
        assertEquals(ReceiptTests.output(bank, ""), receipt.get(i)); i++;

        for (int j = 5; j < 13; j++) {
            assertEquals(ReceiptTests.output(store.getOrder().get(j)), receipt.get(i)); i++;
        }

        assertEquals(i, receipt.size());
    }

    @Test
    protected void invalid_withdraw_transactions_should_output_themselves_after_an_invalid_tag() {
        store.getOrder().add(5, String.format("%s %s %s", TransactionType.Deposit, CHECKING_ID_1, checkingDepositAmount));
        store.getOrder().add(6, String.format("%s %s %s", TransactionType.Deposit, SAVINGS_ID_1, savingsDepositAmount));
        store.getOrder().add(7, String.format("%s %s %s", TransactionType.Withdraw, SAVINGS_ID_1, savingsWithdrawAmount));

        store.getOrder().add(8, String.format("%s %s %s", "", CHECKING_ID_1, checkingWithdrawAmount));
        store.getOrder().add(9, String.format("%s %s %s", "TransactionType.Withdraw", CHECKING_ID_1, checkingWithdrawAmount));
        store.getOrder().add(10, String.format("%s %s %s", TransactionType.Withdraw, "", checkingWithdrawAmount));
        store.getOrder().add(11, String.format("%s %s %s", TransactionType.Withdraw, "CHECKING_ID_1", checkingWithdrawAmount));
        store.getOrder().add(12, String.format("%s %s %s", TransactionType.Withdraw, SAVINGS_ID_1, savingsWithdrawAmount));
        store.getOrder().add(13, String.format("%s %s %s", TransactionType.Withdraw, CD_ID, cdWithdrawAmount));
        store.getOrder().add(14, String.format("%s %s %s", TransactionType.Withdraw, CHECKING_ID_1, ""));
        store.getOrder().add(15, String.format("%s %s %s", TransactionType.Withdraw, CHECKING_ID_1, "checkingWithdrawAmount"));
        store.getOrder().add(16, String.format("%s %s %s", TransactionType.Withdraw, CHECKING_ID_1, 470239));

        List<String> receipt = store.getReceipt();
        int i = 0;
        assertEquals(ReceiptTests.output(bank, CHECKING_ID_1), receipt.get(i)); i++;
        assertEquals(ReceiptTests.output(bank, store.getOrder().get(5)), receipt.get(i)); i++;
        assertEquals(ReceiptTests.output(bank, ""), receipt.get(i)); i++;

        assertEquals(ReceiptTests.output(bank, CHECKING_ID_0), receipt.get(i)); i++;
        assertEquals(ReceiptTests.output(bank, ""), receipt.get(i)); i++;

        assertEquals(ReceiptTests.output(bank, SAVINGS_ID_1), receipt.get(i)); i++;
        assertEquals(ReceiptTests.output(bank, store.getOrder().get(6)), receipt.get(i)); i++;
        assertEquals(ReceiptTests.output(bank, store.getOrder().get(7)), receipt.get(i)); i++;
        assertEquals(ReceiptTests.output(bank, ""), receipt.get(i)); i++;

        assertEquals(ReceiptTests.output(bank, SAVINGS_ID_0), receipt.get(i)); i++;
        assertEquals(ReceiptTests.output(bank, ""), receipt.get(i)); i++;

        assertEquals(ReceiptTests.output(bank, CD_ID), receipt.get(i)); i++;
        assertEquals(ReceiptTests.output(bank, ""), receipt.get(i)); i++;

        for (int j = 8; j < 17; j++) {
            assertEquals(ReceiptTests.output(store.getOrder().get(j)), receipt.get(i)); i++;
        }

        assertEquals(i, receipt.size());
    }

    @Test
    protected void invalid_transfer_transactions_should_output_themselves_after_an_invalid_tag() {
        store.getOrder().add(5, String.format("%s %s %s", TransactionType.Deposit, CHECKING_ID_1, checkingDepositAmount));
        store.getOrder().add(6, String.format("%s %s %s", TransactionType.Deposit, SAVINGS_ID_1, savingsDepositAmount));
        store.getOrder().add(7, String.format("%s %s %s", TransactionType.Withdraw, SAVINGS_ID_1, savingsWithdrawAmount));

        store.getOrder().add(8, String.format("%s %s %s %s", "", CHECKING_ID_1, CHECKING_ID_0, min(checkingWithdrawAmount, checkingDepositAmount)));
        store.getOrder().add(9, String.format("%s %s %s %s", "TransactionType.Transfer", CHECKING_ID_1, SAVINGS_ID_1, min(checkingWithdrawAmount, savingsDepositAmount)));
        store.getOrder().add(10, String.format("%s %s %s %s", TransactionType.Transfer, "", CHECKING_ID_0, min(checkingWithdrawAmount, checkingDepositAmount)));
        store.getOrder().add(11, String.format("%s %s %s %s", TransactionType.Transfer, "CHECKING_ID_1", SAVINGS_ID_1, min(checkingWithdrawAmount, savingsDepositAmount)));
        store.getOrder().add(12, String.format("%s %s %s %s", TransactionType.Transfer, SAVINGS_ID_1, CHECKING_ID_1, min(savingsWithdrawAmount, checkingDepositAmount)));
        store.getOrder().add(13, String.format("%s %s %s %s", TransactionType.Transfer, CD_ID, SAVINGS_ID_1, min(cdWithdrawAmount, savingsDepositAmount)));
        store.getOrder().add(14, String.format("%s %s %s %s", TransactionType.Transfer, CHECKING_ID_1, "", min(checkingWithdrawAmount, checkingDepositAmount)));
        store.getOrder().add(15, String.format("%s %s %s %s", TransactionType.Transfer, CHECKING_ID_1, "SAVINGS_ID_1", min(checkingWithdrawAmount, savingsDepositAmount)));
        store.getOrder().add(16, String.format("%s %s %s %s", TransactionType.Transfer, CHECKING_ID_1, CHECKING_ID_1, min(checkingWithdrawAmount, checkingDepositAmount)));
        store.getOrder().add(17, String.format("%s %s %s %s", TransactionType.Transfer, CHECKING_ID_1, CD_ID, min(checkingWithdrawAmount, checkingDepositAmount)));
        store.getOrder().add(18, String.format("%s %s %s %s", TransactionType.Transfer, CHECKING_ID_1, SAVINGS_ID_1, ""));
        store.getOrder().add(19, String.format("%s %s %s %s", TransactionType.Transfer, CHECKING_ID_1, CHECKING_ID_0, "min(checkingWithdrawAmount, checkingDepositAmount)"));
        store.getOrder().add(20, String.format("%s %s %s %s", TransactionType.Transfer, CHECKING_ID_1, SAVINGS_ID_1, -38974));

        List<String> receipt = store.getReceipt();
        int i = 0;
        assertEquals(ReceiptTests.output(bank, CHECKING_ID_1), receipt.get(i)); i++;
        assertEquals(ReceiptTests.output(bank, store.getOrder().get(5)), receipt.get(i)); i++;
        assertEquals(ReceiptTests.output(bank, ""), receipt.get(i)); i++;

        assertEquals(ReceiptTests.output(bank, CHECKING_ID_0), receipt.get(i)); i++;
        assertEquals(ReceiptTests.output(bank, ""), receipt.get(i)); i++;

        assertEquals(ReceiptTests.output(bank, SAVINGS_ID_1), receipt.get(i)); i++;
        assertEquals(ReceiptTests.output(bank, store.getOrder().get(6)), receipt.get(i)); i++;
        assertEquals(ReceiptTests.output(bank, store.getOrder().get(7)), receipt.get(i)); i++;
        assertEquals(ReceiptTests.output(bank, ""), receipt.get(i)); i++;

        assertEquals(ReceiptTests.output(bank, SAVINGS_ID_0), receipt.get(i)); i++;
        assertEquals(ReceiptTests.output(bank, ""), receipt.get(i)); i++;

        assertEquals(ReceiptTests.output(bank, CD_ID), receipt.get(i)); i++;
        assertEquals(ReceiptTests.output(bank, ""), receipt.get(i)); i++;

        for (int j = 8; j < 21; j++) {
            assertEquals(ReceiptTests.output(store.getOrder().get(j)), receipt.get(i)); i++;
        }

        assertEquals(i, receipt.size());
    }

    @Test
    protected void invalid_time_travel_transactions_should_output_themselves_after_an_invalid_tag() {
        initializeStore();
        TransactionType transactionType = TransactionType.TimeTravel;

        store.getOrder().add(0, String.format("%s %s", "", MONTHS));
        store.getOrder().add(1, String.format("%s %s", "transactionType", MONTHS));
        store.getOrder().add(2, String.format("%s %s", transactionType, ""));
        store.getOrder().add(3, String.format("%s %s", transactionType, "MONTHS"));
        store.getOrder().add(4, String.format("%s %s", transactionType, 789234234));

        List<String> receipt = store.getReceipt();
        int i;
        for (i = 0; i < 5; i++) {
            assertEquals(ReceiptTests.output(store.getOrder().get(i)), receipt.get(i));
        }

        assertEquals(i, receipt.size());
    }

    @Test
    protected void output_should_be_sorted_by_validity_first_id_second_and_time_third() {
        initializeStore();

        store.getOrder().add(0, String.format("%s %s %s", TransactionType.Create, AccountType.SAVINGS, SAVINGS_ID_1));
        store.getOrder().add(1, String.format("%s %s %s", TransactionType.Deposit, SAVINGS_ID_1, 700));

        store.getOrder().add(2, String.format("%s %s %s", TransactionType.Deposit, SAVINGS_ID_1, 5000));

        store.getOrder().add(3, String.format("%s %s %s", TransactionType.Create, AccountType.CHECKING, CHECKING_ID_1));
        store.getOrder().add(4, String.format("%s %s %s", TransactionType.Deposit, CHECKING_ID_1, 300));
        store.getOrder().add(5, String.format("%s %s %s %s", TransactionType.Transfer, CHECKING_ID_1, SAVINGS_ID_1, 300));
        store.getOrder().add(6, String.format("%s %s", TransactionType.TimeTravel, 1));
        store.getOrder().add(7, String.format("%s %s %s %s", TransactionType.Create, AccountType.CD, CD_ID, 2000));

        List<String> receipt = store.getReceipt();
        int i = 0;
        assertEquals(ReceiptTests.output(bank, SAVINGS_ID_1), receipt.get(i)); i++;
        assertEquals(ReceiptTests.output(bank, store.getOrder().get(1)), receipt.get(i)); i++;
        assertEquals(ReceiptTests.output(bank, store.getOrder().get(5)), receipt.get(i)); i++;
        assertEquals(ReceiptTests.output(bank, ""), receipt.get(i)); i++;

        assertEquals(ReceiptTests.output(bank, CHECKING_ID_1), receipt.get(i)); i++;
        assertEquals(ReceiptTests.output(bank, store.getOrder().get(4)), receipt.get(i)); i++;
        assertEquals(ReceiptTests.output(bank, store.getOrder().get(5)), receipt.get(i)); i++;
        assertEquals(ReceiptTests.output(bank, ""), receipt.get(i)); i++;

        assertEquals(ReceiptTests.output(bank, CD_ID), receipt.get(i)); i++;
        assertEquals(ReceiptTests.output(bank, ""), receipt.get(i)); i++;

        assertEquals(ReceiptTests.output(store.getOrder().get(2)), receipt.get(i)); i++;

        assertEquals(i, receipt.size());
    }
}
