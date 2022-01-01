package server.game.pushing.paper.store;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static server.game.pushing.paper.store.BankTests.timeTravel;
import static server.game.pushing.paper.store.bank.AccountType.*;
import static server.game.pushing.paper.store.handler.TransactionType.*;

public class StoreTests {
    private Store store;

    @BeforeEach
    protected void setUp() {
        store = new Store();
    }

    @Test
    protected void a_store_inputs_a_list_of_transactions_called_an_order_and_outputs_a_receipt() {
        List<String> order = new ArrayList<>();

        store.setOrder(order);

        List<String> receipt = store.getReceipt();
        assertTrue(receipt.isEmpty());
    }

    @Test
    protected void create_transactions_output_the_account_type_id_and_balance() {
        String checkingID = "12342345";
        String savingsID = "12345454";
        String cdID = "98439843";
        double cdBalance = 2934;
        List<String> order = new ArrayList<>(Arrays.asList(
                String.format("%s %s %s", Create, Checking, checkingID),
                String.format("%s %s %s", Create, Savings, savingsID),
                String.format("%s %s %s %s", Create, CD, cdID, cdBalance)
        ));

        store.setOrder(order);

        List<String> receipt = store.getReceipt(); int i = 0;
        assertEquals(String.format("%s %s %.2f", Checking, checkingID, 0.).toLowerCase(), receipt.get(i)); i++;
        assertEquals("", receipt.get(i)); i++;
        assertEquals(String.format("%s %s %.2f", Savings, savingsID, 0.).toLowerCase(), receipt.get(i)); i++;
        assertEquals("", receipt.get(i)); i++;
        assertEquals(String.format("%s %s %.2f", CD, cdID, cdBalance).toLowerCase(), receipt.get(i)); i++;
        assertEquals("", receipt.get(i)); i++;
        assertEquals(i, receipt.size());
    }

    @Test
    protected void time_travel_transactions_do_not_have_an_output() {
        String id = "12342345";
        int months = 2;
        double depositAmount = 998;
        List<String> order = new ArrayList<>(Arrays.asList(
                String.format("%s %s %s", Create, Savings, id),
                String.format("%s %s %s", Deposit, id, depositAmount),
                String.format("%s %s", TimeTravel, months)
        ));

        store.setOrder(order);

        List<String> receipt = store.getReceipt(); int i = 0;
        assertEquals(String.format("%s %s %.2f", Savings, id, timeTravel(depositAmount, months)).toLowerCase(), receipt.get(i)); i++;
        assertEquals(String.format("%s %s %.2f", Deposit, id, depositAmount).toLowerCase(), receipt.get(i)); i++;
        assertEquals("", receipt.get(i)); i++;
        assertEquals(i, receipt.size());
    }

    @Test
    protected void deposit_and_withdraw_transactions_output_themselves() {
        String id = "12342345";
        double depositAmount = 998;
        double withdrawAmount = 323;
        List<String> order = new ArrayList<>(Arrays.asList(
                String.format("%s %s %s", Create, Checking, id),
                String.format("%s %s %s", Deposit, id, depositAmount),
                String.format("%s %s %s", Withdraw, id, withdrawAmount)
        ));

        store.setOrder(order);

        List<String> receipt = store.getReceipt(); int i = 0;
        assertEquals(String.format("%s %s %.2f", Checking, id, depositAmount - withdrawAmount).toLowerCase(), receipt.get(i)); i++;
        assertEquals(String.format("%s %s %.2f", Deposit, id, depositAmount).toLowerCase(), receipt.get(i)); i++;
        assertEquals(String.format("%s %s %.2f", Withdraw, id, withdrawAmount).toLowerCase(), receipt.get(i)); i++;
        assertEquals("", receipt.get(i)); i++;
        assertEquals(i, receipt.size());
    }

    @Test
    protected void transfer_transactions_output_themselves_twice() {
        String payingID = "12342345";
        String receivingID = "98989898";
        double depositAmount = 998;
        double transferAmount = 323;
        List<String> order = new ArrayList<>(Arrays.asList(
                String.format("%s %s %s", Create, Savings, payingID),
                String.format("%s %s %s", Deposit, payingID, depositAmount),
                String.format("%s %s %s", Create, Checking, receivingID),
                String.format("%s %s %s %s", Transfer, payingID, receivingID, transferAmount)
        ));

        store.setOrder(order);

        List<String> receipt = store.getReceipt(); int i = 0;
        assertEquals(String.format("%s %s %.2f", Savings, payingID, depositAmount - transferAmount).toLowerCase(), receipt.get(i)); i++;
        assertEquals(String.format("%s %s %.2f", Deposit, payingID, depositAmount).toLowerCase(), receipt.get(i)); i++;
        assertEquals(String.format("%s %s %s %.2f", Transfer, payingID, receivingID, transferAmount).toLowerCase(), receipt.get(i)); i++;
        assertEquals("", receipt.get(i)); i++;
        assertEquals(String.format("%s %s %.2f", Checking, receivingID, transferAmount).toLowerCase(), receipt.get(i)); i++;
        assertEquals(receipt.get(2), receipt.get(i)); i++;
        assertEquals("", receipt.get(i)); i++;
        assertEquals(i, receipt.size());
    }

    @Test
    protected void valid_transactions_output_lowercase_without_extra_arguments() {
        String payingID = "12342345";
        String receivingID = "98989898";
        double depositAmount = 998;
        double transferAmount = 323;
        List<String> order = new ArrayList<>(Arrays.asList(
                String.format("%s %s %s %s %s %s", "create", "SAVINGS", payingID, payingID, payingID, payingID),
                String.format("%s %s %s %s %s %s", "DEPOSIT", payingID, depositAmount, null, false, true),
                String.format("%s %s %s %s %s %s", "CREATE", "checking", receivingID, 234, 345, 125),
                String.format("%s %s %s %s %s %s %s", "transfer", payingID, receivingID, transferAmount, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY, Double.NaN)
        ));

        store.setOrder(order);

        List<String> receipt = store.getReceipt(); int i = 0;
        assertEquals(String.format("%s %s %.2f", Savings, payingID, depositAmount - transferAmount).toLowerCase(), receipt.get(i)); i++;
        assertEquals(String.format("%s %s %.2f", Deposit, payingID, depositAmount).toLowerCase(), receipt.get(i)); i++;
        assertEquals(String.format("%s %s %s %.2f", Transfer, payingID, receivingID, transferAmount).toLowerCase(), receipt.get(i)); i++;
        assertEquals("", receipt.get(i)); i++;
        assertEquals(String.format("%s %s %.2f", Checking, receivingID, transferAmount).toLowerCase(), receipt.get(i)); i++;
        assertEquals(receipt.get(2), receipt.get(i)); i++;
        assertEquals("", receipt.get(i)); i++;
        assertEquals(i, receipt.size());
    }

    @Test
    protected void invalid_transactions_output_themselves_after_an_invalid_tag() {
        List<String> order = new ArrayList<>(Arrays.asList(
                "the power of friendship",
                "the power of love"
        ));

        store.setOrder(order);

        List<String> receipt = store.getReceipt(); int i = 0;
        assertEquals(String.format("%s %s", "[invalid]", order.get(0)), receipt.get(i)); i++;
        assertEquals(String.format("%s %s", "[invalid]", order.get(1)), receipt.get(i)); i++;
        assertEquals(i, receipt.size());
    }

    @Test
    protected void the_output_is_sorted_first_by_validity_second_by_account_third_by_time() {
        String checkingID = "98439843";
        String savingsID = "43343434";
        String cdID = "34983489";
        int months = 2;
        double depositAmount = 900;
        double withdrawAmount = 300;
        double transferAmount = 400;
        List<String> order = new ArrayList<>(Arrays.asList(
                String.format("%s %s %s", Create, Savings, savingsID),
                String.format("%s %s %s", Deposit, savingsID, 5000),
                String.format("%s %s %s", Create, Checking, checkingID),
                String.format("%s %s %s", Deposit, savingsID, depositAmount),
                String.format("%s %s", TimeTravel, months),
                String.format("%s %s %s %s", Create, CD, cdID, 200),
                String.format("%s %s %s %s", Transfer, savingsID, checkingID, transferAmount),
                String.format("%s %s %s", Withdraw, checkingID, withdrawAmount)
        ));

        store.setOrder(order);

        List<String> receipt = store.getReceipt(); int i = 0;
        assertEquals(String.format("%s %s %.2f", Savings, savingsID, (depositAmount - 100 * months) - transferAmount).toLowerCase(), receipt.get(i)); i++;
        assertEquals(String.format("%s %s %.2f", Deposit, savingsID, depositAmount).toLowerCase(), receipt.get(i)); i++;
        assertEquals(String.format("%s %s %s %.2f", Transfer, savingsID, checkingID, transferAmount).toLowerCase(), receipt.get(i)); i++;
        assertEquals("", receipt.get(i)); i++;
        assertEquals(String.format("%s %s %.2f", Checking, checkingID, transferAmount - withdrawAmount).toLowerCase(), receipt.get(i)); i++;
        assertEquals(receipt.get(2), receipt.get(i)); i++;
        assertEquals(String.format("%s %s %.2f", Withdraw, checkingID, withdrawAmount).toLowerCase(), receipt.get(i)); i++;
        assertEquals("", receipt.get(i)); i++;
        assertEquals(String.format("%s %s", "[invalid]", order.get(1)), receipt.get(i)); i++;
        assertEquals(String.format("%s %s", "[invalid]", order.get(5)), receipt.get(i)); i++;
        assertEquals(i, receipt.size());
    }
}
