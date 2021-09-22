package server.game.pushing.paper.store;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.game.pushing.paper.store.bank.Bank;
import server.game.pushing.paper.store.bank.account.AccountType;
import server.game.pushing.paper.store.chain_of_responsibility.TransactionType;

import java.util.Arrays;
import java.util.List;

import static java.lang.Math.min;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static server.game.pushing.paper.store.ReceiptTests.outputInvalid;
import static server.game.pushing.paper.store.ReceiptTests.outputValid;
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
    private double apr;
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
        apr = bank.getMaxAPR();
        bank.createChecking(CHECKING_ID_1, apr);
        bank.createSavings(SAVINGS_ID_1, apr);
        bank.createCD(CD_ID, apr, initialCDBalance);
        initialCDBalance = bank.getMinInitialCDBalance();
        checkingDepositAmount = bank.getAccount(CHECKING_ID_1).getMaxDepositAmount();
        savingsDepositAmount = bank.getAccount(SAVINGS_ID_1).getMaxDepositAmount();
        checkingWithdrawAmount = bank.getAccount(CHECKING_ID_1).getMaxWithdrawAmount();
        savingsWithdrawAmount = bank.getAccount(SAVINGS_ID_1).getMaxWithdrawAmount();
        cdWithdrawAmount = bank.getAccount(CD_ID).getMaxWithdrawAmount();

        initializeStore();
        store.setOrder(Arrays.asList(
                String.format("%s %s %s %s", transactionType, AccountType.Checking, CHECKING_ID_1, apr),
                String.format("%s %s %s %s", transactionType, AccountType.Checking, CHECKING_ID_0, apr),
                String.format("%s %s %s %s", transactionType, AccountType.Savings, SAVINGS_ID_1, apr),
                String.format("%s %s %s %s", transactionType, AccountType.Savings, SAVINGS_ID_0, apr),
                String.format("%s %s %s %s %s", transactionType, AccountType.CD, CD_ID, apr, initialCDBalance)
        ));
    }

    private void initializeStore() {
        store = new Store();
        bank = store.getBank();
    }

    @Test
    protected void empty_order_should_output_empty_receipt() {
        initializeStore();

        List<String> receipt = store.getReceipt();
        assertTrue(receipt.isEmpty());
    }

    @Test
    protected void valid_create_order_should_output_account_type_id_apr_and_balance() {
        List<String> receipt = store.getReceipt();
        int i = 0;
        assertEquals(outputValid(bank, CHECKING_ID_1), receipt.get(i)); i++;
        assertEquals(outputValid(bank, ""), receipt.get(i)); i++;

        assertEquals(outputValid(bank, CHECKING_ID_0), receipt.get(i)); i++;
        assertEquals(outputValid(bank, ""), receipt.get(i)); i++;

        assertEquals(outputValid(bank, SAVINGS_ID_1), receipt.get(i)); i++;
        assertEquals(outputValid(bank, ""), receipt.get(i)); i++;

        assertEquals(outputValid(bank, SAVINGS_ID_0), receipt.get(i)); i++;
        assertEquals(outputValid(bank, ""), receipt.get(i)); i++;

        assertEquals(outputValid(bank, CD_ID), receipt.get(i)); i++;
        assertEquals(outputValid(bank, ""), receipt.get(i)); i++;

        assertEquals(i, receipt.size());
    }

    @Test
    protected void valid_deposit_order_should_output_itself() {
        TransactionType transactionType = TransactionType.Deposit;

        store.getOrder().add(5, String.format("%s %s %s", transactionType, CHECKING_ID_1, checkingDepositAmount));
        store.getOrder().add(6, String.format("%s %s %s", transactionType, SAVINGS_ID_1, savingsDepositAmount));

        List<String> receipt = store.getReceipt();
        int i = 0;
        assertEquals(outputValid(bank, CHECKING_ID_1), receipt.get(i)); i++;
        assertEquals(outputValid(bank, store.getOrder().get(5)), receipt.get(i)); i++;
        assertEquals(outputValid(bank, ""), receipt.get(i)); i++;

        assertEquals(outputValid(bank, CHECKING_ID_0), receipt.get(i)); i++;
        assertEquals(outputValid(bank, ""), receipt.get(i)); i++;

        assertEquals(outputValid(bank, SAVINGS_ID_1), receipt.get(i)); i++;
        assertEquals(outputValid(bank, store.getOrder().get(6)), receipt.get(i)); i++;
        assertEquals(outputValid(bank, ""), receipt.get(i)); i++;

        assertEquals(outputValid(bank, SAVINGS_ID_0), receipt.get(i)); i++;
        assertEquals(outputValid(bank, ""), receipt.get(i)); i++;

        assertEquals(outputValid(bank, CD_ID), receipt.get(i)); i++;
        assertEquals(outputValid(bank, ""), receipt.get(i)); i++;

        assertEquals(i, receipt.size());
    }

    @Test
    protected void valid_withdraw_order_should_output_itself() {
        store.getOrder().add(5, String.format("%s %s %s", TransactionType.Deposit, CHECKING_ID_1, checkingDepositAmount));
        store.getOrder().add(6, String.format("%s %s %s", TransactionType.Deposit, SAVINGS_ID_1, savingsDepositAmount));
        store.getOrder().add(7, String.format("%s %s", TransactionType.PassTime, MONTHS));
        store.getOrder().add(8, String.format("%s %s %s", TransactionType.Withdraw, CHECKING_ID_1, checkingWithdrawAmount));
        store.getOrder().add(9, String.format("%s %s %s", TransactionType.Withdraw, SAVINGS_ID_1, savingsWithdrawAmount));
        store.getOrder().add(10, String.format("%s %s %s", TransactionType.Withdraw, CD_ID, cdWithdrawAmount));

        List<String> receipt = store.getReceipt();
        int i = 0;
        assertEquals(outputValid(bank, CHECKING_ID_1), receipt.get(i)); i++;
        assertEquals(outputValid(bank, store.getOrder().get(5)), receipt.get(i)); i++;
        assertEquals(outputValid(bank, store.getOrder().get(8)), receipt.get(i)); i++;
        assertEquals(outputValid(bank, ""), receipt.get(i)); i++;

        assertEquals(outputValid(bank, SAVINGS_ID_1), receipt.get(i)); i++;
        assertEquals(outputValid(bank, store.getOrder().get(6)), receipt.get(i)); i++;
        assertEquals(outputValid(bank, store.getOrder().get(9)), receipt.get(i)); i++;
        assertEquals(outputValid(bank, ""), receipt.get(i)); i++;

        assertEquals(outputValid(bank, CD_ID), receipt.get(i)); i++;
        assertEquals(outputValid(bank, store.getOrder().get(10)), receipt.get(i)); i++;
        assertEquals(outputValid(bank, ""), receipt.get(i)); i++;

        assertEquals(i, receipt.size());
    }

    @Test
    protected void valid_transfer_order_should_output_itself_twice() {
        store.getOrder().add(5, String.format("%s %s %s", TransactionType.Deposit, CHECKING_ID_1, checkingDepositAmount));
        store.getOrder().add(6, String.format("%s %s %s", TransactionType.Deposit, CHECKING_ID_0, checkingDepositAmount));
        store.getOrder().add(7, String.format("%s %s %s", TransactionType.Deposit, SAVINGS_ID_1, savingsDepositAmount));
        store.getOrder().add(8, String.format("%s %s %s", TransactionType.Deposit, SAVINGS_ID_0, savingsDepositAmount));
        store.getOrder().add(9, String.format("%s %s", TransactionType.PassTime, MONTHS));
        store.getOrder().add(10, String.format("%s %s %s %s", TransactionType.Transfer, CHECKING_ID_1, CHECKING_ID_0, min(checkingWithdrawAmount, checkingDepositAmount)));
        store.getOrder().add(11, String.format("%s %s %s %s", TransactionType.Transfer, CHECKING_ID_1, SAVINGS_ID_1, min(checkingWithdrawAmount, savingsDepositAmount)));
        store.getOrder().add(12, String.format("%s %s %s %s", TransactionType.Transfer, SAVINGS_ID_1, CHECKING_ID_1, min(savingsWithdrawAmount, checkingDepositAmount)));
        store.getOrder().add(13, String.format("%s %s %s %s", TransactionType.Transfer, SAVINGS_ID_0, SAVINGS_ID_1, min(savingsWithdrawAmount, savingsDepositAmount)));
        store.getOrder().add(14, String.format("%s %s %s %s", TransactionType.Transfer, CD_ID, SAVINGS_ID_1, min(cdWithdrawAmount, savingsDepositAmount)));

        List<String> receipt = store.getReceipt();
        int i = 0;
        assertEquals(outputValid(bank, CHECKING_ID_1), receipt.get(i)); i++;
        assertEquals(outputValid(bank, store.getOrder().get(5)), receipt.get(i)); i++;
        assertEquals(outputValid(bank, store.getOrder().get(10)), receipt.get(i)); i++;
        assertEquals(outputValid(bank, store.getOrder().get(11)), receipt.get(i)); i++;
        assertEquals(outputValid(bank, store.getOrder().get(12)), receipt.get(i)); i++;
        assertEquals(outputValid(bank, ""), receipt.get(i)); i++;

        assertEquals(outputValid(bank, CHECKING_ID_0), receipt.get(i)); i++;
        assertEquals(outputValid(bank, store.getOrder().get(6)), receipt.get(i)); i++;
        assertEquals(outputValid(bank, store.getOrder().get(10)), receipt.get(i)); i++;
        assertEquals(outputValid(bank, ""), receipt.get(i)); i++;

        assertEquals(outputValid(bank, SAVINGS_ID_1), receipt.get(i)); i++;
        assertEquals(outputValid(bank, store.getOrder().get(7)), receipt.get(i)); i++;
        assertEquals(outputValid(bank, store.getOrder().get(11)), receipt.get(i)); i++;
        assertEquals(outputValid(bank, store.getOrder().get(12)), receipt.get(i)); i++;
        assertEquals(outputValid(bank, store.getOrder().get(13)), receipt.get(i)); i++;
        assertEquals(outputValid(bank, store.getOrder().get(14)), receipt.get(i)); i++;
        assertEquals(outputValid(bank, ""), receipt.get(i)); i++;

        assertEquals(outputValid(bank, SAVINGS_ID_0), receipt.get(i)); i++;
        assertEquals(outputValid(bank, store.getOrder().get(8)), receipt.get(i)); i++;
        assertEquals(outputValid(bank, store.getOrder().get(13)), receipt.get(i)); i++;
        assertEquals(outputValid(bank, ""), receipt.get(i)); i++;

        assertEquals(outputValid(bank, CD_ID), receipt.get(i)); i++;
        assertEquals(outputValid(bank, store.getOrder().get(14)), receipt.get(i)); i++;
        assertEquals(outputValid(bank, ""), receipt.get(i)); i++;

        assertEquals(i, receipt.size());
    }

    @Test
    protected void valid_pass_time_order_should_not_output() {
        store.getOrder().add(5, String.format("%s %s %s", TransactionType.Deposit, CHECKING_ID_1, checkingDepositAmount));
        store.getOrder().add(6, String.format("%s %s %s", TransactionType.Deposit, SAVINGS_ID_1, savingsDepositAmount));
        store.getOrder().add(7, String.format("%s %s", TransactionType.PassTime, MONTHS));

        List<String> receipt = store.getReceipt();
        int i = 0;
        assertEquals(outputValid(bank, CHECKING_ID_1), receipt.get(i)); i++;
        assertEquals(outputValid(bank, store.getOrder().get(5)), receipt.get(i)); i++;
        assertEquals(outputValid(bank, ""), receipt.get(i)); i++;

        assertEquals(outputValid(bank, SAVINGS_ID_1), receipt.get(i)); i++;
        assertEquals(outputValid(bank, store.getOrder().get(6)), receipt.get(i)); i++;
        assertEquals(outputValid(bank, ""), receipt.get(i)); i++;

        assertEquals(outputValid(bank, CD_ID), receipt.get(i)); i++;
        assertEquals(outputValid(bank, ""), receipt.get(i)); i++;

        assertEquals(i, receipt.size());
    }

    @Test
    protected void valid_order_should_output_lower_case_and_trim_useless_additional_arguments() {
        store.getOrder().add(5, String.format("%s %s %s %s %s %s %s", "dEpOsIt", SAVINGS_ID_0, savingsDepositAmount, "the", "power", "of", "friendship"));
        store.getOrder().add(6, String.format("%s %s %s %s %s %s %s %s", "tRAnSFeR", SAVINGS_ID_0, CHECKING_ID_1, min(savingsWithdrawAmount, checkingDepositAmount), store, 1, minBalanceFee, min(437, 234)));
        store.getOrder().add(7, String.format("%s %s %s %s %s %s %s %s    %s %s %s    %s %s %s    %s %s %s %s %s %s %s %s %s %s %s %s   %s %s %s   %s %s %s", "paSS tImE", MONTHS, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, "last one"));
        store.getOrder().add(8, String.format("%s %s %s %s        ", "wiTHdRAW", SAVINGS_ID_0, savingsWithdrawAmount, 0));

        List<String> receipt = store.getReceipt();
        int i = 0;
        assertEquals(outputValid(bank, CHECKING_ID_1), receipt.get(i)); i++;
        assertEquals(outputValid(bank, store.getOrder().get(6), 4), receipt.get(i)); i++;
        assertEquals(outputValid(bank, ""), receipt.get(i)); i++;

        assertEquals(outputValid(bank, SAVINGS_ID_0), receipt.get(i)); i++;
        assertEquals(outputValid(bank, store.getOrder().get(5), 3), receipt.get(i)); i++;
        assertEquals(outputValid(bank, store.getOrder().get(6), 4), receipt.get(i)); i++;
        assertEquals(outputValid(bank, store.getOrder().get(8), 3), receipt.get(i)); i++;
        assertEquals(outputValid(bank, ""), receipt.get(i)); i++;

        assertEquals(outputValid(bank, CD_ID), receipt.get(i)); i++;
        assertEquals(outputValid(bank, ""), receipt.get(i)); i++;

        assertEquals(i, receipt.size());
    }

    @Test
    protected void invalid_create_order_should_output_itself_as_invalid() {
        initializeStore();
        TransactionType transactionType = TransactionType.Create;

        store.getOrder().add(0, String.format("%s %s %s %s", "", AccountType.Checking, CHECKING_ID_1, apr));
        store.getOrder().add(1, String.format("%s %s %s %s", "transactionType", AccountType.Savings, SAVINGS_ID_1, apr));
        store.getOrder().add(2, String.format("%s %s %s %s %s", transactionType, "", CD_ID, apr, initialCDBalance));
        store.getOrder().add(3, String.format("%s %s %s %s", transactionType, "AccountType.Savings", CHECKING_ID_1, apr));
        store.getOrder().add(4, String.format("%s %s %s %s", transactionType, AccountType.Savings, "", apr));
        store.getOrder().add(5, String.format("%s %s %s %s %s", transactionType, AccountType.CD, "SAVINGS_ID_0", apr, initialCDBalance));
        store.getOrder().add(6, String.format("%s %s %s %s", transactionType, AccountType.Checking, CHECKING_ID_1, ""));
        store.getOrder().add(7, String.format("%s %s %s %s", transactionType, AccountType.Savings, SAVINGS_ID_1, "apr"));
        store.getOrder().add(8, String.format("%s %s %s %s %s", transactionType, AccountType.CD, CD_ID, 235789, initialCDBalance));
        store.getOrder().add(9, String.format("%s %s %s %s %s", transactionType, AccountType.CD, CD_ID, apr, ""));
        store.getOrder().add(10, String.format("%s %s %s %s %s", transactionType, AccountType.CD, CD_ID, apr, "initialCDBalance"));
        store.getOrder().add(11, String.format("%s %s %s %s %s", transactionType, AccountType.CD, CD_ID, apr, -178234));

        List<String> receipt = store.getReceipt();
        int i;
        for (i = 0; i < 12; i++) {
            assertEquals(outputInvalid(store.getOrder().get(i)), receipt.get(i));
        }

        assertEquals(i, receipt.size());
    }

    @Test
    protected void invalid_deposit_order_should_output_itself_as_invalid() {
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
        assertEquals(outputValid(bank, CHECKING_ID_1), receipt.get(i)); i++;
        assertEquals(outputValid(bank, ""), receipt.get(i)); i++;

        assertEquals(outputValid(bank, CHECKING_ID_0), receipt.get(i)); i++;
        assertEquals(outputValid(bank, ""), receipt.get(i)); i++;

        assertEquals(outputValid(bank, SAVINGS_ID_1), receipt.get(i)); i++;
        assertEquals(outputValid(bank, ""), receipt.get(i)); i++;

        assertEquals(outputValid(bank, SAVINGS_ID_0), receipt.get(i)); i++;
        assertEquals(outputValid(bank, ""), receipt.get(i)); i++;

        assertEquals(outputValid(bank, CD_ID), receipt.get(i)); i++;
        assertEquals(outputValid(bank, ""), receipt.get(i)); i++;

        for (int j = 5; j < 13; j++) {
            assertEquals(outputInvalid(store.getOrder().get(j)), receipt.get(i)); i++;
        }

        assertEquals(i, receipt.size());
    }

    @Test
    protected void invalid_withdraw_order_should_output_itself_as_invalid() {
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
        assertEquals(outputValid(bank, CHECKING_ID_1), receipt.get(i)); i++;
        assertEquals(outputValid(bank, store.getOrder().get(5)), receipt.get(i)); i++;
        assertEquals(outputValid(bank, ""), receipt.get(i)); i++;

        assertEquals(outputValid(bank, CHECKING_ID_0), receipt.get(i)); i++;
        assertEquals(outputValid(bank, ""), receipt.get(i)); i++;

        assertEquals(outputValid(bank, SAVINGS_ID_1), receipt.get(i)); i++;
        assertEquals(outputValid(bank, store.getOrder().get(6)), receipt.get(i)); i++;
        assertEquals(outputValid(bank, store.getOrder().get(7)), receipt.get(i)); i++;
        assertEquals(outputValid(bank, ""), receipt.get(i)); i++;

        assertEquals(outputValid(bank, SAVINGS_ID_0), receipt.get(i)); i++;
        assertEquals(outputValid(bank, ""), receipt.get(i)); i++;

        assertEquals(outputValid(bank, CD_ID), receipt.get(i)); i++;
        assertEquals(outputValid(bank, ""), receipt.get(i)); i++;

        for (int j = 8; j < 17; j++) {
            assertEquals(outputInvalid(store.getOrder().get(j)), receipt.get(i)); i++;
        }

        assertEquals(i, receipt.size());
    }

    @Test
    protected void invalid_transfer_order_should_output_itself_as_invalid() {
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
        assertEquals(outputValid(bank, CHECKING_ID_1), receipt.get(i)); i++;
        assertEquals(outputValid(bank, store.getOrder().get(5)), receipt.get(i)); i++;
        assertEquals(outputValid(bank, ""), receipt.get(i)); i++;

        assertEquals(outputValid(bank, CHECKING_ID_0), receipt.get(i)); i++;
        assertEquals(outputValid(bank, ""), receipt.get(i)); i++;

        assertEquals(outputValid(bank, SAVINGS_ID_1), receipt.get(i)); i++;
        assertEquals(outputValid(bank, store.getOrder().get(6)), receipt.get(i)); i++;
        assertEquals(outputValid(bank, store.getOrder().get(7)), receipt.get(i)); i++;
        assertEquals(outputValid(bank, ""), receipt.get(i)); i++;

        assertEquals(outputValid(bank, SAVINGS_ID_0), receipt.get(i)); i++;
        assertEquals(outputValid(bank, ""), receipt.get(i)); i++;

        assertEquals(outputValid(bank, CD_ID), receipt.get(i)); i++;
        assertEquals(outputValid(bank, ""), receipt.get(i)); i++;

        for (int j = 8; j < 21; j++) {
            assertEquals(outputInvalid(store.getOrder().get(j)), receipt.get(i)); i++;
        }

        assertEquals(i, receipt.size());
    }

    @Test
    protected void invalid_pass_time_order_should_output_as_invalid() {
        initializeStore();
        TransactionType transactionType = TransactionType.PassTime;

        store.getOrder().add(0, String.format("%s %s", "", MONTHS));
        store.getOrder().add(1, String.format("%s %s", "transactionType", MONTHS));
        store.getOrder().add(2, String.format("%s %s", transactionType, ""));
        store.getOrder().add(3, String.format("%s %s", transactionType, "MONTHS"));
        store.getOrder().add(4, String.format("%s %s", transactionType, 789234234));

        List<String> receipt = store.getReceipt();
        int i;
        for (i = 0; i < 5; i++) {
            assertEquals(outputInvalid(store.getOrder().get(i)), receipt.get(i));
        }

        assertEquals(i, receipt.size());
    }

    @Test
    protected void order_should_output_by_validity_id_and_then_time() {
        initializeStore();

        store.getOrder().add(0, String.format("%s %s %s %s", TransactionType.Create, AccountType.Savings, SAVINGS_ID_1, 0.6));
        store.getOrder().add(1, String.format("%s %s %s", TransactionType.Deposit, SAVINGS_ID_1, 700));

        store.getOrder().add(2, String.format("%s %s %s", TransactionType.Deposit, SAVINGS_ID_1, 5000));

        store.getOrder().add(3, String.format("%s %s %s %s", TransactionType.Create, AccountType.Checking, CHECKING_ID_1, 0.01));
        store.getOrder().add(4, String.format("%s %s %s", TransactionType.Deposit, CHECKING_ID_1, 300));
        store.getOrder().add(5, String.format("%s %s %s %s", TransactionType.Transfer, CHECKING_ID_1, SAVINGS_ID_1, 300));
        store.getOrder().add(6, String.format("%s %s", TransactionType.PassTime, 1));
        store.getOrder().add(7, String.format("%s %s %s %s %s", TransactionType.Create, AccountType.CD, CD_ID, 1.2, 2000));

        List<String> receipt = store.getReceipt();
        int i = 0;
        assertEquals(outputValid(bank, SAVINGS_ID_1), receipt.get(i)); i++;
        assertEquals(outputValid(bank, store.getOrder().get(1)), receipt.get(i)); i++;
        assertEquals(outputValid(bank, store.getOrder().get(5)), receipt.get(i)); i++;
        assertEquals(outputValid(bank, ""), receipt.get(i)); i++;

        assertEquals(outputValid(bank, CD_ID), receipt.get(i)); i++;
        assertEquals(outputValid(bank, ""), receipt.get(i)); i++;

        assertEquals(outputInvalid(store.getOrder().get(2)), receipt.get(i)); i++;

        assertEquals(i, receipt.size());
    }

    @Test
    protected void output_when_state_change_should_update() {
        valid_create_order_should_output_account_type_id_apr_and_balance();
        valid_deposit_order_should_output_itself();
    }
}
