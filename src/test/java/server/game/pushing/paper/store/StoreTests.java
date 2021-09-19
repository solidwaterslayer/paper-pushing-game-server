package server.game.pushing.paper.store;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.game.pushing.paper.store.bank.Bank;
import server.game.pushing.paper.store.bank.account.Account;
import server.game.pushing.paper.store.bank.account.AccountType;
import server.game.pushing.paper.store.chainofresponsibility.TransactionType;

import java.util.ArrayList;
import java.util.List;

import static java.lang.Math.min;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static server.game.pushing.paper.store.bank.Bank.getMonthsPerYear;

public class StoreTests {
    private List<String> order;
    private Store store;

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
        order = new ArrayList<>();
        store = new Store();
        Bank bank = store.getBank();

        TransactionType transactionType = TransactionType.Create;
        minBalanceFee = bank.getMinBalanceFee();
        apr = bank.getMaxAPR();
        initialCDBalance = bank.getMinInitialCDBalance();
        order.add(0, String.format("%s %s %s %s", transactionType, AccountType.Checking, CHECKING_ID_1, apr));
        order.add(1, String.format("%s %s %s %s", transactionType, AccountType.Checking, CHECKING_ID_0, apr));
        order.add(2, String.format("%s %s %s %s", transactionType, AccountType.Savings, SAVINGS_ID_1, apr));
        order.add(3, String.format("%s %s %s %s", transactionType, AccountType.Savings, SAVINGS_ID_0, apr));
        order.add(4, String.format("%s %s %s %s %s", transactionType, AccountType.CD, CD_ID, apr, initialCDBalance));
        store.order(order);
        bank = store.getBank();
        checkingDepositAmount = bank.getAccount(CHECKING_ID_1).getMaxDepositAmount();
        savingsDepositAmount = bank.getAccount(SAVINGS_ID_1).getMaxDepositAmount();
        checkingWithdrawAmount = bank.getAccount(CHECKING_ID_1).getMaxWithdrawAmount();
        savingsWithdrawAmount = bank.getAccount(SAVINGS_ID_1).getMaxWithdrawAmount();
        cdWithdrawAmount = bank.getAccount(CD_ID).getMaxWithdrawAmount();
    }

    @Test
    protected void empty_order_should_output_empty_receipt() {
        order = new ArrayList<>();

        List<String> receipt = store.order(order);
        assertEquals(0, receipt.size());
    }

    @Test
    protected void valid_create_order_should_output_account_type_id_apr_and_balance() {
        List<String> receipt = store.order(order);
        int i = 0;
        assertEquals(outputValid(CHECKING_ID_1), receipt.get(i)); i++;
        assertEquals(outputValid(""), receipt.get(i)); i++;

        assertEquals(outputValid(CHECKING_ID_0), receipt.get(i)); i++;
        assertEquals(outputValid(""), receipt.get(i)); i++;

        assertEquals(outputValid(SAVINGS_ID_1), receipt.get(i)); i++;
        assertEquals(outputValid(""), receipt.get(i)); i++;

        assertEquals(outputValid(SAVINGS_ID_0), receipt.get(i)); i++;
        assertEquals(outputValid(""), receipt.get(i)); i++;

        assertEquals(outputValid(CD_ID), receipt.get(i)); i++;
        assertEquals(outputValid(""), receipt.get(i)); i++;

        assertEquals(i, receipt.size());
    }

    @Test
    protected void valid_deposit_checking_order_should_output_itself() {
        TransactionType transactionType = TransactionType.Deposit;

        order.add(5, String.format("%s %s %s", transactionType, CHECKING_ID_1, checkingDepositAmount));

        List<String> receipt = store.order(order);
        int i = 0;
        assertEquals(outputValid(CHECKING_ID_1), receipt.get(i)); i++;
        assertEquals(outputValid(order.get(5)), receipt.get(i)); i++;
        assertEquals(outputValid(""), receipt.get(i)); i++;

        assertEquals(outputValid(CHECKING_ID_0), receipt.get(i)); i++;
        assertEquals(outputValid(""), receipt.get(i)); i++;

        assertEquals(outputValid(SAVINGS_ID_1), receipt.get(i)); i++;
        assertEquals(outputValid(""), receipt.get(i)); i++;

        assertEquals(outputValid(SAVINGS_ID_0), receipt.get(i)); i++;
        assertEquals(outputValid(""), receipt.get(i)); i++;

        assertEquals(outputValid(CD_ID), receipt.get(i)); i++;
        assertEquals(outputValid(""), receipt.get(i)); i++;

        assertEquals(i, receipt.size());
    }

    @Test
    protected void valid_deposit_savings_order_should_output_itself() {
        TransactionType transactionType = TransactionType.Deposit;

        order.add(5, String.format("%s %s %s", transactionType, SAVINGS_ID_1, savingsDepositAmount));

        List<String> receipt = store.order(order);
        int i = 0;
        assertEquals(outputValid(CHECKING_ID_1), receipt.get(i)); i++;
        assertEquals(outputValid(""), receipt.get(i)); i++;

        assertEquals(outputValid(CHECKING_ID_0), receipt.get(i)); i++;
        assertEquals(outputValid(""), receipt.get(i)); i++;

        assertEquals(outputValid(SAVINGS_ID_1), receipt.get(i)); i++;
        assertEquals(outputValid(order.get(5)), receipt.get(i)); i++;
        assertEquals(outputValid(""), receipt.get(i)); i++;

        assertEquals(outputValid(SAVINGS_ID_0), receipt.get(i)); i++;
        assertEquals(outputValid(""), receipt.get(i)); i++;

        assertEquals(outputValid(CD_ID), receipt.get(i)); i++;
        assertEquals(outputValid(""), receipt.get(i)); i++;

        assertEquals(i, receipt.size());
    }

    @Test
    protected void valid_withdraw_order_should_output_itself() {
        order.add(5, String.format("%s %s %s", TransactionType.Deposit, CHECKING_ID_1, checkingDepositAmount));
        order.add(6, String.format("%s %s %s", TransactionType.Deposit, SAVINGS_ID_1, savingsDepositAmount));
        order.add(7, String.format("%s %s", TransactionType.PassTime, MONTHS));
        order.add(8, String.format("%s %s %s", TransactionType.Withdraw, CHECKING_ID_1, checkingWithdrawAmount));
        order.add(9, String.format("%s %s %s", TransactionType.Withdraw, SAVINGS_ID_1, savingsWithdrawAmount));
        order.add(10, String.format("%s %s %s", TransactionType.Withdraw, CD_ID, cdWithdrawAmount));

        List<String> receipt = store.order(order);
        int i = 0;
        assertEquals(outputValid(CHECKING_ID_1), receipt.get(i)); i++;
        assertEquals(outputValid(order.get(5)), receipt.get(i)); i++;
        assertEquals(outputValid(order.get(8)), receipt.get(i)); i++;
        assertEquals(outputValid(""), receipt.get(i)); i++;

        assertEquals(outputValid(SAVINGS_ID_1), receipt.get(i)); i++;
        assertEquals(outputValid(order.get(6)), receipt.get(i)); i++;
        assertEquals(outputValid(order.get(9)), receipt.get(i)); i++;
        assertEquals(outputValid(""), receipt.get(i)); i++;

        assertEquals(outputValid(CD_ID), receipt.get(i)); i++;
        assertEquals(outputValid(order.get(10)), receipt.get(i)); i++;
        assertEquals(outputValid(""), receipt.get(i)); i++;

        assertEquals(i, receipt.size());
    }

    @Test
    protected void valid_transfer_order_should_output_itself_twice() {
        order.add(5, String.format("%s %s %s", TransactionType.Deposit, CHECKING_ID_1, checkingDepositAmount));
        order.add(6, String.format("%s %s %s", TransactionType.Deposit, CHECKING_ID_0, checkingDepositAmount));
        order.add(7, String.format("%s %s %s", TransactionType.Deposit, SAVINGS_ID_1, savingsDepositAmount));
        order.add(8, String.format("%s %s %s", TransactionType.Deposit, SAVINGS_ID_0, savingsDepositAmount));
        order.add(9, String.format("%s %s", TransactionType.PassTime, MONTHS));
        order.add(10, String.format("%s %s %s %s", TransactionType.Transfer, CHECKING_ID_1, CHECKING_ID_0, min(checkingWithdrawAmount, checkingDepositAmount)));
        order.add(11, String.format("%s %s %s %s", TransactionType.Transfer, CHECKING_ID_1, SAVINGS_ID_1, min(checkingWithdrawAmount, savingsDepositAmount)));
        order.add(12, String.format("%s %s %s %s", TransactionType.Transfer, SAVINGS_ID_1, CHECKING_ID_1, min(savingsWithdrawAmount, checkingDepositAmount)));
        order.add(13, String.format("%s %s %s %s", TransactionType.Transfer, SAVINGS_ID_0, SAVINGS_ID_1, min(savingsWithdrawAmount, savingsDepositAmount)));
        order.add(14, String.format("%s %s %s %s", TransactionType.Transfer, CD_ID, SAVINGS_ID_1, min(cdWithdrawAmount, savingsDepositAmount)));

        List<String> receipt = store.order(order);
        int i = 0;
        assertEquals(outputValid(CHECKING_ID_1), receipt.get(i)); i++;
        assertEquals(outputValid(order.get(5)), receipt.get(i)); i++;
        assertEquals(outputValid(order.get(10)), receipt.get(i)); i++;
        assertEquals(outputValid(order.get(11)), receipt.get(i)); i++;
        assertEquals(outputValid(order.get(12)), receipt.get(i)); i++;
        assertEquals(outputValid(""), receipt.get(i)); i++;

        assertEquals(outputValid(CHECKING_ID_0), receipt.get(i)); i++;
        assertEquals(outputValid(order.get(6)), receipt.get(i)); i++;
        assertEquals(outputValid(order.get(10)), receipt.get(i)); i++;
        assertEquals(outputValid(""), receipt.get(i)); i++;

        assertEquals(outputValid(SAVINGS_ID_1), receipt.get(i)); i++;
        assertEquals(outputValid(order.get(7)), receipt.get(i)); i++;
        assertEquals(outputValid(order.get(11)), receipt.get(i)); i++;
        assertEquals(outputValid(order.get(12)), receipt.get(i)); i++;
        assertEquals(outputValid(order.get(13)), receipt.get(i)); i++;
        assertEquals(outputValid(order.get(14)), receipt.get(i)); i++;
        assertEquals(outputValid(""), receipt.get(i)); i++;

        assertEquals(outputValid(SAVINGS_ID_0), receipt.get(i)); i++;
        assertEquals(outputValid(order.get(8)), receipt.get(i)); i++;
        assertEquals(outputValid(order.get(13)), receipt.get(i)); i++;
        assertEquals(outputValid(""), receipt.get(i)); i++;

        assertEquals(outputValid(CD_ID), receipt.get(i)); i++;
        assertEquals(outputValid(order.get(14)), receipt.get(i)); i++;
        assertEquals(outputValid(""), receipt.get(i)); i++;

        assertEquals(i, receipt.size());
    }

    @Test
    protected void valid_pass_time_order_should_not_output() {
        order.add(5, String.format("%s %s %s", TransactionType.Deposit, CHECKING_ID_1, checkingDepositAmount));
        order.add(6, String.format("%s %s %s", TransactionType.Deposit, SAVINGS_ID_1, savingsDepositAmount));
        order.add(7, String.format("%s %s", TransactionType.PassTime, MONTHS));

        List<String> receipt = store.order(order);
        int i = 0;
        assertEquals(outputValid(CHECKING_ID_1), receipt.get(i)); i++;
        assertEquals(outputValid(order.get(5)), receipt.get(i)); i++;
        assertEquals(outputValid(""), receipt.get(i)); i++;

        assertEquals(outputValid(SAVINGS_ID_1), receipt.get(i)); i++;
        assertEquals(outputValid(order.get(6)), receipt.get(i)); i++;
        assertEquals(outputValid(""), receipt.get(i)); i++;

        assertEquals(outputValid(CD_ID), receipt.get(i)); i++;
        assertEquals(outputValid(""), receipt.get(i)); i++;

        assertEquals(i, receipt.size());
    }

    @Test
    protected void valid_order_should_output_lower_case_and_trim_useless_additional_arguments() {
        order.add(5, String.format("%s %s %s %s %s %s %s", "dEpOsIt", SAVINGS_ID_0, savingsDepositAmount, "the", "power", "of", "friendship"));
        order.add(6, String.format("%s %s %s %s %s %s %s %s", "tRAnSFeR", SAVINGS_ID_0, CHECKING_ID_1, min(savingsWithdrawAmount, checkingDepositAmount), store, 1, minBalanceFee, min(437, 234)));
        order.add(7, String.format("%s %s %s %s %s %s %s %s    %s %s %s    %s %s %s    %s %s %s %s %s %s %s %s %s %s %s %s   %s %s %s   %s %s %s", "paSS tImE", MONTHS, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, "last one"));
        order.add(8, String.format("%s %s %s %s        ", "wiTHdRAW", SAVINGS_ID_0, savingsWithdrawAmount, 0));

        List<String> receipt = store.order(order);
        int i = 0;
        assertEquals(outputValid(CHECKING_ID_1), receipt.get(i)); i++;
        assertEquals(outputValid(order.get(6), 4), receipt.get(i)); i++;
        assertEquals(outputValid(""), receipt.get(i)); i++;

        assertEquals(outputValid(SAVINGS_ID_0), receipt.get(i)); i++;
        assertEquals(outputValid(order.get(5), 3), receipt.get(i)); i++;
        assertEquals(outputValid(order.get(6), 4), receipt.get(i)); i++;
        assertEquals(outputValid(order.get(8), 3), receipt.get(i)); i++;
        assertEquals(outputValid(""), receipt.get(i)); i++;

        assertEquals(outputValid(CD_ID), receipt.get(i)); i++;
        assertEquals(outputValid(""), receipt.get(i)); i++;

        assertEquals(i, receipt.size());
    }

    private String outputValid(String string) {
        Bank bank = store.getBank();
        if (bank.containsAccount(string)) {
            Account account = bank.getAccount(string);
            return String.format("%s %s %.2f %.2f", account.getAccountType(), account.getID(), account.getAPR(), account.getBalance()).toLowerCase();
        }

        return string.toLowerCase();
    }

    private String outputValid(String transaction, int trim) {
        return outputValid(transaction.split(" "), trim);
    }

    private String outputValid(String[] transactionArguments, int trim) {
        StringBuilder beautifiedTransaction = new StringBuilder();

        for(int i = 0; i < trim; i++){
            beautifiedTransaction.append(transactionArguments[i]).append(" ");
        }

        return outputValid(beautifiedTransaction.substring(0, beautifiedTransaction.length() - 1));
    }

    @Test
    protected void invalid_create_order_should_output_itself_as_invalid() {
        order = new ArrayList<>();
        TransactionType transactionType = TransactionType.Create;

        order.add(0, String.format("%s %s %s %s", "", AccountType.Checking, CHECKING_ID_1, apr));
        order.add(1, String.format("%s %s %s %s", "transactionType", AccountType.Savings, SAVINGS_ID_1, apr));
        order.add(2, String.format("%s %s %s %s %s", transactionType, "", CD_ID, apr, initialCDBalance));
        order.add(3, String.format("%s %s %s %s", transactionType, "AccountType.Savings", CHECKING_ID_1, apr));
        order.add(4, String.format("%s %s %s %s", transactionType, AccountType.Savings, "", apr));
        order.add(5, String.format("%s %s %s %s %s", transactionType, AccountType.CD, "SAVINGS_ID_0", apr, initialCDBalance));
        order.add(6, String.format("%s %s %s %s", transactionType, AccountType.Checking, CHECKING_ID_1, ""));
        order.add(7, String.format("%s %s %s %s", transactionType, AccountType.Savings, SAVINGS_ID_1, "apr"));
        order.add(8, String.format("%s %s %s %s %s", transactionType, AccountType.CD, CD_ID, 235789, initialCDBalance));
        order.add(9, String.format("%s %s %s %s %s", transactionType, AccountType.CD, CD_ID, apr, ""));
        order.add(10, String.format("%s %s %s %s %s", transactionType, AccountType.CD, CD_ID, apr, "initialCDBalance"));
        order.add(11, String.format("%s %s %s %s %s", transactionType, AccountType.CD, CD_ID, apr, -178234));

        List<String> receipt = store.order(order);
        int i;
        for (i = 0; i < 12; i++) {
            assertEquals(outputInvalid(order.get(i)), receipt.get(i));
        }

        assertEquals(i, receipt.size());
    }

    @Test
    protected void invalid_deposit_order_should_output_itself_as_invalid() {
        TransactionType transactionType = TransactionType.Deposit;

        order.add(5, String.format("%s %s %s", "", CHECKING_ID_1, checkingDepositAmount));
        order.add(6, String.format("%s %s %s", "TransactionType.Deposit", SAVINGS_ID_1, savingsDepositAmount));
        order.add(7, String.format("%s %s %s", transactionType, "", checkingDepositAmount));
        order.add(8, String.format("%s %s %s", transactionType, "SAVINGS_ID_1", savingsDepositAmount));
        order.add(9, String.format("%s %s %s", transactionType, CD_ID, checkingDepositAmount));
        order.add(10, String.format("%s %s %s", transactionType, SAVINGS_ID_1, ""));
        order.add(11, String.format("%s %s %s", transactionType, CHECKING_ID_1, "savingsDepositAmount"));
        order.add(12, String.format("%s %s %s", transactionType, SAVINGS_ID_1, 23487984));

        List<String> receipt = store.order(order);
        int i = 0;
        assertEquals(outputValid(CHECKING_ID_1), receipt.get(i)); i++;
        assertEquals(outputValid(""), receipt.get(i)); i++;

        assertEquals(outputValid(CHECKING_ID_0), receipt.get(i)); i++;
        assertEquals(outputValid(""), receipt.get(i)); i++;

        assertEquals(outputValid(SAVINGS_ID_1), receipt.get(i)); i++;
        assertEquals(outputValid(""), receipt.get(i)); i++;

        assertEquals(outputValid(SAVINGS_ID_0), receipt.get(i)); i++;
        assertEquals(outputValid(""), receipt.get(i)); i++;

        assertEquals(outputValid(CD_ID), receipt.get(i)); i++;
        assertEquals(outputValid(""), receipt.get(i)); i++;

        for (int j = 5; j < 13; j++) {
            assertEquals(outputInvalid(order.get(j)), receipt.get(i)); i++;
        }

        assertEquals(i, receipt.size());
    }

    @Test
    protected void invalid_withdraw_order_should_output_itself_as_invalid() {
        order.add(5, String.format("%s %s %s", TransactionType.Deposit, CHECKING_ID_1, checkingDepositAmount));
        order.add(6, String.format("%s %s %s", TransactionType.Deposit, SAVINGS_ID_1, savingsDepositAmount));
        order.add(7, String.format("%s %s %s", TransactionType.Withdraw, SAVINGS_ID_1, savingsWithdrawAmount));

        order.add(8, String.format("%s %s %s", "", CHECKING_ID_1, checkingWithdrawAmount));
        order.add(9, String.format("%s %s %s", "TransactionType.Withdraw", CHECKING_ID_1, checkingWithdrawAmount));
        order.add(10, String.format("%s %s %s", TransactionType.Withdraw, "", checkingWithdrawAmount));
        order.add(11, String.format("%s %s %s", TransactionType.Withdraw, "CHECKING_ID_1", checkingWithdrawAmount));
        order.add(12, String.format("%s %s %s", TransactionType.Withdraw, SAVINGS_ID_1, savingsWithdrawAmount));
        order.add(13, String.format("%s %s %s", TransactionType.Withdraw, CD_ID, cdWithdrawAmount));
        order.add(14, String.format("%s %s %s", TransactionType.Withdraw, CHECKING_ID_1, ""));
        order.add(15, String.format("%s %s %s", TransactionType.Withdraw, CHECKING_ID_1, "checkingWithdrawAmount"));
        order.add(16, String.format("%s %s %s", TransactionType.Withdraw, CHECKING_ID_1, 470239));

        List<String> receipt = store.order(order);
        int i = 0;
        assertEquals(outputValid(CHECKING_ID_1), receipt.get(i)); i++;
        assertEquals(outputValid(order.get(5)), receipt.get(i)); i++;
        assertEquals(outputValid(""), receipt.get(i)); i++;

        assertEquals(outputValid(CHECKING_ID_0), receipt.get(i)); i++;
        assertEquals(outputValid(""), receipt.get(i)); i++;

        assertEquals(outputValid(SAVINGS_ID_1), receipt.get(i)); i++;
        assertEquals(outputValid(order.get(6)), receipt.get(i)); i++;
        assertEquals(outputValid(order.get(7)), receipt.get(i)); i++;
        assertEquals(outputValid(""), receipt.get(i)); i++;

        assertEquals(outputValid(SAVINGS_ID_0), receipt.get(i)); i++;
        assertEquals(outputValid(""), receipt.get(i)); i++;

        assertEquals(outputValid(CD_ID), receipt.get(i)); i++;
        assertEquals(outputValid(""), receipt.get(i)); i++;

        for (int j = 8; j < 17; j++) {
            assertEquals(outputInvalid(order.get(j)), receipt.get(i)); i++;
        }

        assertEquals(i, receipt.size());
    }

    @Test
    protected void invalid_transfer_order_should_output_itself_as_invalid() {
        order.add(5, String.format("%s %s %s", TransactionType.Deposit, CHECKING_ID_1, checkingDepositAmount));
        order.add(6, String.format("%s %s %s", TransactionType.Deposit, SAVINGS_ID_1, savingsDepositAmount));
        order.add(7, String.format("%s %s %s", TransactionType.Withdraw, SAVINGS_ID_1, savingsWithdrawAmount));

        order.add(8, String.format("%s %s %s %s", "", CHECKING_ID_1, CHECKING_ID_0, min(checkingWithdrawAmount, checkingDepositAmount)));
        order.add(9, String.format("%s %s %s %s", "TransactionType.Transfer", CHECKING_ID_1, SAVINGS_ID_1, min(checkingWithdrawAmount, savingsDepositAmount)));
        order.add(10, String.format("%s %s %s %s", TransactionType.Transfer, "", CHECKING_ID_0, min(checkingWithdrawAmount, checkingDepositAmount)));
        order.add(11, String.format("%s %s %s %s", TransactionType.Transfer, "CHECKING_ID_1", SAVINGS_ID_1, min(checkingWithdrawAmount, savingsDepositAmount)));
        order.add(12, String.format("%s %s %s %s", TransactionType.Transfer, SAVINGS_ID_1, CHECKING_ID_1, min(savingsWithdrawAmount, checkingDepositAmount)));
        order.add(13, String.format("%s %s %s %s", TransactionType.Transfer, CD_ID, SAVINGS_ID_1, min(cdWithdrawAmount, savingsDepositAmount)));
        order.add(14, String.format("%s %s %s %s", TransactionType.Transfer, CHECKING_ID_1, "", min(checkingWithdrawAmount, checkingDepositAmount)));
        order.add(15, String.format("%s %s %s %s", TransactionType.Transfer, CHECKING_ID_1, "SAVINGS_ID_1", min(checkingWithdrawAmount, savingsDepositAmount)));
        order.add(16, String.format("%s %s %s %s", TransactionType.Transfer, CHECKING_ID_1, CHECKING_ID_1, min(checkingWithdrawAmount, checkingDepositAmount)));
        order.add(17, String.format("%s %s %s %s", TransactionType.Transfer, CHECKING_ID_1, CD_ID, min(checkingWithdrawAmount, checkingDepositAmount)));
        order.add(18, String.format("%s %s %s %s", TransactionType.Transfer, CHECKING_ID_1, SAVINGS_ID_1, ""));
        order.add(19, String.format("%s %s %s %s", TransactionType.Transfer, CHECKING_ID_1, CHECKING_ID_0, "min(checkingWithdrawAmount, checkingDepositAmount)"));
        order.add(20, String.format("%s %s %s %s", TransactionType.Transfer, CHECKING_ID_1, SAVINGS_ID_1, -38974));

        List<String> receipt = store.order(order);
        int i = 0;
        assertEquals(outputValid(CHECKING_ID_1), receipt.get(i)); i++;
        assertEquals(outputValid(order.get(5)), receipt.get(i)); i++;
        assertEquals(outputValid(""), receipt.get(i)); i++;

        assertEquals(outputValid(CHECKING_ID_0), receipt.get(i)); i++;
        assertEquals(outputValid(""), receipt.get(i)); i++;

        assertEquals(outputValid(SAVINGS_ID_1), receipt.get(i)); i++;
        assertEquals(outputValid(order.get(6)), receipt.get(i)); i++;
        assertEquals(outputValid(order.get(7)), receipt.get(i)); i++;
        assertEquals(outputValid(""), receipt.get(i)); i++;

        assertEquals(outputValid(SAVINGS_ID_0), receipt.get(i)); i++;
        assertEquals(outputValid(""), receipt.get(i)); i++;

        assertEquals(outputValid(CD_ID), receipt.get(i)); i++;
        assertEquals(outputValid(""), receipt.get(i)); i++;

        for (int j = 8; j < 21; j++) {
            assertEquals(outputInvalid(order.get(j)), receipt.get(i)); i++;
        }

        assertEquals(i, receipt.size());
    }

    @Test
    protected void invalid_pass_time_order_should_output_as_invalid() {
        order = new ArrayList<>();
        TransactionType transactionType = TransactionType.PassTime;

        order.add(0, String.format("%s %s", "", MONTHS));
        order.add(1, String.format("%s %s", "transactionType", MONTHS));
        order.add(2, String.format("%s %s", transactionType, ""));
        order.add(3, String.format("%s %s", transactionType, "MONTHS"));
        order.add(4, String.format("%s %s", transactionType, 789234234));

        List<String> receipt = store.order(order);
        int i;
        for (i = 0; i < 5; i++) {
            assertEquals(outputInvalid(order.get(i)), receipt.get(i));
        }

        assertEquals(i, receipt.size());
    }

    @Test
    protected void order_should_output_by_validity_id_and_then_time() {
        order = new ArrayList<>();

        order.add(0, String.format("%s %s %s %s", TransactionType.Create, AccountType.Savings, SAVINGS_ID_1, 0.6));
        order.add(1, String.format("%s %s %s", TransactionType.Deposit, SAVINGS_ID_1, 700));
        order.add(2, String.format("%s %s %s", TransactionType.Deposit, SAVINGS_ID_1, 5000));
        order.add(3, String.format("%s %s %s %s", TransactionType.Create, AccountType.Checking, CHECKING_ID_1, 0.01));
        order.add(4, String.format("%s %s %s", TransactionType.Deposit, CHECKING_ID_1, 300));
        order.add(5, String.format("%s %s %s %s", TransactionType.Transfer, CHECKING_ID_1, SAVINGS_ID_1, 300));
        order.add(6, String.format("%s %s", TransactionType.PassTime, 1));
        order.add(7, String.format("%s %s %s %s %s", TransactionType.Create, AccountType.CD, CD_ID, 1.2, 2000));

        List<String> receipt = store.order(order);
        int i = 0;
        assertEquals(outputValid(SAVINGS_ID_1), receipt.get(i)); i++;
        assertEquals(outputValid(order.get(1)), receipt.get(i)); i++;
        assertEquals(outputValid(order.get(5)), receipt.get(i)); i++;
        assertEquals(outputValid(""), receipt.get(i)); i++;

        assertEquals(outputValid(CD_ID), receipt.get(i)); i++;
        assertEquals(outputValid(""), receipt.get(i)); i++;

        assertEquals(outputInvalid(order.get(2)), receipt.get(i)); i++;

        assertEquals(i, receipt.size());
    }

    private String outputInvalid(String transaction) {
        return "[invalid] " + transaction;
    }
}
