package server.game.pushing.paper.store.chain_of_responsibility.transaction_validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.game.pushing.paper.store.bank.Bank;
import server.game.pushing.paper.store.chain_of_responsibility.ChainOfResponsibility;
import server.game.pushing.paper.store.chain_of_responsibility.TransactionType;

import java.util.Arrays;
import java.util.List;

import static java.lang.Math.min;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class DepositValidatorTests {
    private Bank bank;
    private ChainOfResponsibility validator;

    private TransactionType transactionType;
    private final String CHECKING_ID = "09096564";
    private final String SAVINGS_ID = "90438954";
    private final String CD_ID = "98430842";

    @BeforeEach
    protected void setUp() {
        bank = new Bank();
        validator = new DepositValidator(bank);

        transactionType = validator.getTransactionType();
        double startingCDBalance = bank.getMinStartingCDBalance();

        bank.createChecking(CHECKING_ID);
        bank.createSavings(SAVINGS_ID);
        bank.createCD(CD_ID, startingCDBalance);
    }

    @Test
    protected void the_first_argument_of_deposit_transactions_is_the_transaction_type_deposit() {
        String id = SAVINGS_ID;
        double depositAmount = bank.getAccount(id).getMaxDepositAmount();

        assertFalse(validator.handle(""));
        assertFalse(validator.handle(String.format("%s %s %s", "", "", "")));
        assertFalse(validator.handle(String.format("%s %s %s", "", id, depositAmount)));
        assertFalse(validator.handle(String.format("%s %s %s", "nuke", id, depositAmount)));
        assertTrue(validator.handle(String.format("%s %s %s", transactionType, id, depositAmount)));
    }

    @Test
    protected void the_second_argument_of_deposit_transactions_is_a_taken_id() {
        String id = SAVINGS_ID;
        double depositAmount = bank.getAccount(id).getMaxDepositAmount();

        assertFalse(validator.handle(String.format("%s %s %s", transactionType, "", "")));
        assertFalse(validator.handle(String.format("%s %s %s", transactionType, "", depositAmount)));
        assertFalse(validator.handle(String.format("%s %s %s", transactionType, "87439742", depositAmount)));
        assertTrue(validator.handle(String.format("%s %s %s", transactionType, id, depositAmount)));
    }

    @Test
    protected void the_third_argument_of_deposit_transactions_is_a_deposit_amount() {
        String id = SAVINGS_ID;
        double depositAmount = bank.getAccount(id).getMaxDepositAmount();

        assertFalse(validator.handle(String.format("%s %s %s", transactionType, id, "")));
        assertFalse(validator.handle(String.format("%s %s %s", transactionType, id, "depositAmount")));
        assertTrue(validator.handle(String.format("%s %s %s", transactionType, id, depositAmount)));
    }

    @Test
    protected void deposit_amounts_to_checking_accounts_should_be_greater_than_0() {
        String id = CHECKING_ID;
        double depositAmount = 0;

        assertFalse(validator.handle(String.format("%s %s %s", transactionType, id, depositAmount - 1000)));
        assertFalse(validator.handle(String.format("%s %s %s", transactionType, id, depositAmount - 1)));
        assertFalse(validator.handle(String.format("%s %s %s", transactionType, id, depositAmount)));
        assertTrue(validator.handle(String.format("%s %s %s", transactionType, id, depositAmount + 1)));
        assertTrue(validator.handle(String.format("%s %s %s", transactionType, id, 500)));
    }

    @Test
    protected void deposit_amounts_to_checking_accounts_should_be_less_than_or_equal_to_1000() {
        String id = CHECKING_ID;
        double depositAmount = 1000;

        assertTrue(validator.handle(String.format("%s %s %s", transactionType, id, 600)));
        assertTrue(validator.handle(String.format("%s %s %s", transactionType, id, depositAmount - 1)));
        assertTrue(validator.handle(String.format("%s %s %s", transactionType, id, depositAmount)));
        assertFalse(validator.handle(String.format("%s %s %s", transactionType, id, depositAmount + 1)));
        assertFalse(validator.handle(String.format("%s %s %s", transactionType, id, depositAmount + 1000)));
    }

    @Test
    protected void deposit_amounts_to_savings_accounts_should_be_greater_than_0() {
        String id = SAVINGS_ID;
        double depositAmount = 0;

        assertFalse(validator.handle(String.format("%s %s %s", transactionType, id, depositAmount - 1000)));
        assertFalse(validator.handle(String.format("%s %s %s", transactionType, id, depositAmount - 1)));
        assertFalse(validator.handle(String.format("%s %s %s", transactionType, id, depositAmount)));
        assertTrue(validator.handle(String.format("%s %s %s", transactionType, id, depositAmount + 1)));
        assertTrue(validator.handle(String.format("%s %s %s", transactionType, id, 500)));
    }

    @Test
    protected void deposit_amounts_to_savings_accounts_should_be_less_than_or_equal_to_2500() {
        String id = SAVINGS_ID;
        double depositAmount = 2500;

        assertTrue(validator.handle(String.format("%s %s %s", transactionType, id, 600)));
        assertTrue(validator.handle(String.format("%s %s %s", transactionType, id, depositAmount - 1)));
        assertTrue(validator.handle(String.format("%s %s %s", transactionType, id, depositAmount)));
        assertFalse(validator.handle(String.format("%s %s %s", transactionType, id, depositAmount + 1)));
        assertFalse(validator.handle(String.format("%s %s %s", transactionType, id, depositAmount + 1000)));
    }

    @Test
    protected void deposit_amounts_to_cd_accounts_are_invalid() {
        List<Double> depositAmounts = Arrays.asList(-1000.0, -1.0, 0.0, 1.0, 1200.0, 1300.0, 2400.0, 2500.0, 2501.0, 3500.0);

        for (Double depositAmount : depositAmounts) {
            assertFalse(validator.handle(String.format("%s %s %s", transactionType, CD_ID, depositAmount)));
        }
    }

    @Test
    protected void deposit_validators_can_ignore_additional_arguments() {
        assertTrue(validator.handle(String.format("%s %s %s %s", transactionType, CHECKING_ID, bank.getAccount(CHECKING_ID).getMaxDepositAmount(), "nuke")));
        assertTrue(validator.handle(String.format("%s %s %s %s %s %s  %s %s  %s %s", transactionType, SAVINGS_ID, bank.getAccount(SAVINGS_ID).getMaxDepositAmount(), "0", "0", "0", "0", "0", "0", "0")));
    }

    @Test
    protected void deposit_validators_are_insensitive() {
        assertTrue(validator.handle(String.format("dePoSIT %s %s", CHECKING_ID, bank.getAccount(CHECKING_ID).getMaxDepositAmount())));
        assertTrue(validator.handle(String.format("deposit %s %s", SAVINGS_ID, bank.getAccount(SAVINGS_ID).getMaxDepositAmount())));
    }

    @Test
    protected void deposit_validators_can_be_in_a_chain_of_responsibility() {
        String payingID = CHECKING_ID;
        String receivingID = SAVINGS_ID;
        double withdrawAmount = bank.getAccount(payingID).getMaxWithdrawAmount();
        double transferAmount = min(bank.getAccount(payingID).getMaxWithdrawAmount(), bank.getAccount(receivingID).getMaxDepositAmount());

        validator.setNext(new WithdrawValidator(bank));

        assertTrue(validator.handle(String.format("%s %s %s", TransactionType.Withdraw, payingID, withdrawAmount)));
        assertFalse(validator.handle(String.format("%s %s %s %s", TransactionType.Transfer, payingID, receivingID, transferAmount)));
    }
}
