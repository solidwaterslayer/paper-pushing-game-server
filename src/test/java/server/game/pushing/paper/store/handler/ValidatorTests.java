package server.game.pushing.paper.store.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.game.pushing.paper.store.bank.AccountType;
import server.game.pushing.paper.store.bank.Bank;
import server.game.pushing.paper.store.handler.validator.*;

import static java.lang.Math.min;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static server.game.pushing.paper.store.bank.AccountType.*;
import static server.game.pushing.paper.store.bank.Bank.getMonthsPerYear;

public class ValidatorTests {
    private Bank bank;
    private Handler createValidator;
    private Handler timeTravelValidator;
    private Handler depositValidator;
    private Handler withdrawValidator;
    private Handler transferValidator;

    private final String payingID = "98430842";
    private final String receivingID = "97842849";

    @BeforeEach
    protected void setUp() {
        bank = new Bank();
        createValidator = new CreateValidator(bank);
        timeTravelValidator = new TimeTravelValidator(bank);
        depositValidator = new DepositValidator(bank);
        withdrawValidator = new WithdrawValidator(bank);
        transferValidator = new TransferValidator(bank);

        bank.createCheckingAccount(payingID);
        bank.createSavingsAccount(receivingID);
    }

    @Test
    protected void the_first_argument_in_a_create_transaction_is_the_transaction_type_create() {
        TransactionType transactionType = createValidator.getTransactionType();
        AccountType accountType = Checking;
        String id = "87658765";

        assertFalse(createValidator.handleTransaction(""));
        assertFalse(createValidator.handleTransaction(String.format("%s %s %s", "", "", "")));
        assertFalse(createValidator.handleTransaction(String.format("%s %s %s", "", accountType, id)));
        assertFalse(createValidator.handleTransaction(String.format("%s %s %s", "nuke", accountType, id)));
        assertTrue(createValidator.handleTransaction(String.format("%s %s %s", transactionType, accountType, id)));
    }

    @Test
    protected void the_second_argument_in_a_create_transaction_is_a_valid_account_type() {
        TransactionType transactionType = createValidator.getTransactionType();
        String id = "85858585";
        double cdBalance = bank.getMinCDBalance();

        assertFalse(createValidator.handleTransaction(String.format("%s %s %s", transactionType, "", "")));
        assertFalse(createValidator.handleTransaction(String.format("%s %s %s", transactionType, "", id)));
        assertFalse(createValidator.handleTransaction(String.format("%s %s %s", transactionType, "love", id)));
        assertTrue(createValidator.handleTransaction(String.format("%s %s %s", transactionType, Checking, id)));
        assertTrue(createValidator.handleTransaction(String.format("%s %s %s", transactionType, Savings, id)));
        assertTrue(createValidator.handleTransaction(String.format("%s %s %s %s", transactionType, CD, id, cdBalance)));
    }

    @Test
    protected void the_third_argument_in_a_create_transaction_is_a_valid_id() {
        TransactionType transactionType = createValidator.getTransactionType();
        AccountType accountType = Savings;
        String id = "56675667";

        assertFalse(createValidator.handleTransaction(String.format("%s %s %s", transactionType, accountType, "")));
        assertFalse(createValidator.handleTransaction(String.format("%s %s %s", transactionType, accountType, "the powerpuff girls")));
        assertTrue(createValidator.handleTransaction(String.format("%s %s %s", transactionType, accountType, id)));
    }

    @Test
    protected void the_fourth_argument_in_a_create_cd_transaction_is_a_valid_balance() {
        TransactionType transactionType = createValidator.getTransactionType();
        AccountType accountType = CD;
        String id = "87658765";
        double cdBalance = bank.getMinCDBalance();

        assertFalse(createValidator.handleTransaction(String.format("%s %s %s %s", transactionType, accountType, id, "")));
        assertFalse(createValidator.handleTransaction(String.format("%s %s %s %s", transactionType, accountType, id, "smoke air everyday")));
        assertTrue(createValidator.handleTransaction(String.format("%s %s %s %s", transactionType, accountType, id, cdBalance)));
    }

    @Test
    protected void the_first_and_second_argument_in_a_time_travel_transaction_is_the_transaction_type_time_travel() {
        TransactionType transactionType = timeTravelValidator.getTransactionType();
        int months = getMonthsPerYear();

        assertFalse(timeTravelValidator.handleTransaction(""));
        assertFalse(timeTravelValidator.handleTransaction(String.format("%s %s", "", "")));
        assertFalse(timeTravelValidator.handleTransaction(String.format("%s %s", "", months)));
        assertFalse(timeTravelValidator.handleTransaction(String.format("%s %s", "two words", months)));
        assertTrue(timeTravelValidator.handleTransaction(String.format("%s %s", transactionType, months)));
    }

    @Test
    protected void the_second_argument_in_a_time_travel_transaction_are_valid_months() {
        TransactionType transactionType = timeTravelValidator.getTransactionType();
        int months = getMonthsPerYear();

        assertFalse(timeTravelValidator.handleTransaction(String.format("%s %s", transactionType, "")));
        assertFalse(timeTravelValidator.handleTransaction(String.format("%s %s", transactionType, "months")));
        assertTrue(timeTravelValidator.handleTransaction(String.format("%s %s", transactionType, months)));
    }

    @Test
    protected void the_first_argument_in_a_deposit_transaction_is_the_transaction_type_deposit() {
        TransactionType transactionType = depositValidator.getTransactionType();
        String id = payingID;
        double depositAmount = bank.getAccount(id).getMaxDepositAmount();

        assertFalse(depositValidator.handleTransaction(""));
        assertFalse(depositValidator.handleTransaction(String.format("%s %s %s", "", "", "")));
        assertFalse(depositValidator.handleTransaction(String.format("%s %s %s", "", id, depositAmount)));
        assertFalse(depositValidator.handleTransaction(String.format("%s %s %s", "nuke", id, depositAmount)));
        assertTrue(depositValidator.handleTransaction(String.format("%s %s %s", transactionType, id, depositAmount)));
    }

    @Test
    protected void the_second_argument_in_a_deposit_transaction_is_a_valid_id() {
        TransactionType transactionType = depositValidator.getTransactionType();
        String id = payingID;
        double depositAmount = bank.getAccount(id).getMaxDepositAmount();

        assertFalse(depositValidator.handleTransaction(String.format("%s %s %s", transactionType, "", "")));
        assertFalse(depositValidator.handleTransaction(String.format("%s %s %s", transactionType, "", depositAmount)));
        assertFalse(depositValidator.handleTransaction(String.format("%s %s %s", transactionType, "tree", depositAmount)));
        assertTrue(depositValidator.handleTransaction(String.format("%s %s %s", transactionType, id, depositAmount)));
    }

    @Test
    protected void the_third_argument_in_a_deposit_transaction_is_a_valid_deposit_amount() {
        TransactionType transactionType = depositValidator.getTransactionType();
        String id = payingID;
        double depositAmount = bank.getAccount(id).getMaxDepositAmount();

        assertFalse(depositValidator.handleTransaction(String.format("%s %s %s", transactionType, id, "")));
        assertFalse(depositValidator.handleTransaction(String.format("%s %s %s", transactionType, id, "depositAmount")));
        assertTrue(depositValidator.handleTransaction(String.format("%s %s %s", transactionType, id, depositAmount)));
    }

    @Test
    protected void the_first_argument_in_a_withdraw_transaction_is_the_transaction_type_withdraw() {
        TransactionType transactionType = withdrawValidator.getTransactionType();
        String id = receivingID;
        double withdrawAmount = bank.getAccount(id).getMaxWithdrawAmount();

        assertFalse(withdrawValidator.handleTransaction(""));
        assertFalse(withdrawValidator.handleTransaction(String.format("%s %s %s", "", "", "")));
        assertFalse(withdrawValidator.handleTransaction(String.format("%s %s %s", "", id, withdrawAmount)));
        assertFalse(withdrawValidator.handleTransaction(String.format("%s %s %s", "nuke", id, withdrawAmount)));
        assertTrue(withdrawValidator.handleTransaction(String.format("%s %s %s", transactionType, id, withdrawAmount)));
    }

    @Test
    protected void the_second_argument_in_a_withdraw_transaction_is_a_valid_id() {
        TransactionType transactionType = withdrawValidator.getTransactionType();
        String id = receivingID;
        double withdrawAmount = bank.getAccount(id).getMaxWithdrawAmount();

        assertFalse(withdrawValidator.handleTransaction(String.format("%s %s %s", transactionType, "", "")));
        assertFalse(withdrawValidator.handleTransaction(String.format("%s %s %s", transactionType, "", withdrawAmount)));
        assertFalse(withdrawValidator.handleTransaction(String.format("%s %s %s", transactionType, "bob", withdrawAmount)));
        assertTrue(withdrawValidator.handleTransaction(String.format("%s %s %s", transactionType, id, withdrawAmount)));
    }

    @Test
    protected void the_third_argument_in_a_withdraw_transaction_is_a_valid_withdraw_amount() {
        TransactionType transactionType = withdrawValidator.getTransactionType();
        String id = receivingID;
        double withdrawAmount = bank.getAccount(id).getMaxWithdrawAmount();

        assertFalse(withdrawValidator.handleTransaction(String.format("%s %s %s", transactionType, id, "")));
        assertFalse(withdrawValidator.handleTransaction(String.format("%s %s %s", transactionType, id, Double.POSITIVE_INFINITY)));
        assertTrue(withdrawValidator.handleTransaction(String.format("%s %s %s", transactionType, id, withdrawAmount)));
    }

    @Test
    protected void the_first_argument_in_a_transfer_transaction_is_the_transaction_type_transfer() {
        TransactionType transactionType = transferValidator.getTransactionType();
        String payingID = this.payingID;
        String receivingID = this.receivingID;
        double transferAmount = min(bank.getAccount(payingID).getMaxWithdrawAmount(), bank.getAccount(receivingID).getMaxDepositAmount());

        assertFalse(transferValidator.handleTransaction(""));
        assertFalse(transferValidator.handleTransaction(String.format("%s %s %s %s", "", "", "", "")));
        assertFalse(transferValidator.handleTransaction(String.format("%s %s %s %s", "", payingID, receivingID, transferAmount)));
        assertFalse(transferValidator.handleTransaction(String.format("%s %s %s %s", "nuke", payingID, receivingID, transferAmount)));
        assertTrue(transferValidator.handleTransaction(String.format("%s %s %s %s", transactionType, payingID, receivingID, transferAmount)));
    }

    @Test
    protected void the_second_and_third_argument_in_a_transfer_transaction_are_a_valid_paying_id_and_receiving_id() {
        TransactionType transactionType = transferValidator.getTransactionType();
        String payingID = this.payingID;
        String receivingID = this.receivingID;
        double transferAmount = min(bank.getAccount(payingID).getMaxWithdrawAmount(), bank.getAccount(receivingID).getMaxDepositAmount());

        assertFalse(transferValidator.handleTransaction(String.format("%s %s %s %s", transactionType, "", "", "")));
        assertFalse(transferValidator.handleTransaction(String.format("%s %s %s %s", transactionType, "", "", transactionType)));
        assertFalse(transferValidator.handleTransaction(String.format("%s %s %s %s", transactionType, payingID, payingID, transferAmount)));
        assertTrue(transferValidator.handleTransaction(String.format("%s %s %s %s", transactionType, payingID, receivingID, transferAmount)));
    }

    @Test
    protected void the_fourth_argument_in_a_transfer_transaction_is_a_valid_transfer_amount() {
        TransactionType transactionType = transferValidator.getTransactionType();
        String payingID = this.payingID;
        String receivingID = this.receivingID;
        double transferAmount = min(bank.getAccount(payingID).getMaxWithdrawAmount(), bank.getAccount(receivingID).getMaxDepositAmount());

        assertFalse(transferValidator.handleTransaction(String.format("%s %s %s %s", transactionType, payingID, receivingID, "")));
        assertFalse(transferValidator.handleTransaction(String.format("%s %s %s %s", transactionType, payingID, receivingID, "the powerpuff girls")));
        assertTrue(transferValidator.handleTransaction(String.format("%s %s %s %s", transactionType, payingID, receivingID, transferAmount)));
    }
}
