package server.game.pushing.paper.store.chain_of_responsibility.transaction_validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.game.pushing.paper.store.bank.Bank;
import server.game.pushing.paper.store.chain_of_responsibility.ChainOfResponsibility;
import server.game.pushing.paper.store.chain_of_responsibility.TransactionType;

import static java.lang.Math.min;
import static org.junit.jupiter.api.Assertions.*;
import static server.game.pushing.paper.store.bank.Bank.getMonthsPerYear;
import static server.game.pushing.paper.store.bank.BankTests.timeTravel;

public class WithdrawValidatorTests {
    private Bank bank;
    private ChainOfResponsibility validator;

    private final int MONTHS = getMonthsPerYear();
    private TransactionType transactionType;
    private final String CHECKING_ID = "34782479";
    private final String SAVINGS_ID = "98430842";
    private final String CD_ID = "43784268";
    private double startingCDBalance;

    @BeforeEach
    protected void setUp() {
        bank = new Bank();
        validator = new WithdrawValidator(bank);

        transactionType = validator.getTransactionType();
        startingCDBalance = bank.getMinStartingCDBalance();

        bank.createChecking(CHECKING_ID);
        bank.createSavings(SAVINGS_ID);
        bank.createCD(CD_ID, startingCDBalance);
        bank.deposit(CHECKING_ID, bank.getAccount(CHECKING_ID).getMaxDepositAmount());
        bank.deposit(SAVINGS_ID, bank.getAccount(SAVINGS_ID).getMaxDepositAmount());
    }

    @Test
    protected void the_first_argument_of_withdraw_transactions_is_the_transaction_type_withdraw() {
        String id = CHECKING_ID;
        double withdrawAmount = bank.getAccount(id).getMaxWithdrawAmount();

        assertFalse(validator.handle(""));
        assertFalse(validator.handle(String.format("%s %s %s", "", "", "")));
        assertFalse(validator.handle(String.format("%s %s %s", "", id, withdrawAmount)));
        assertFalse(validator.handle(String.format("%s %s %s", "nuke", id, withdrawAmount)));
        assertTrue(validator.handle(String.format("%s %s %s", transactionType, id, withdrawAmount)));
    }

    @Test
    protected void the_second_argument_of_withdraw_transactions_is_a_taken_id() {
        String id = SAVINGS_ID;
        double withdrawAmount = bank.getAccount(id).getMaxWithdrawAmount();

        assertFalse(validator.handle(String.format("%s %s %s", transactionType, "", "")));
        assertFalse(validator.handle(String.format("%s %s %s", transactionType, "", withdrawAmount)));
        assertFalse(validator.handle(String.format("%s %s %s", transactionType, "87439742", withdrawAmount)));
        assertTrue(validator.handle(String.format("%s %s %s", transactionType, id, withdrawAmount)));
    }

    @Test
    protected void the_third_argument_of_withdraw_transactions_is_a_withdraw_amount() {
        String id = CD_ID;
        double withdrawAmount = bank.getAccount(id).getMaxWithdrawAmount();

        bank.timeTravel(getMonthsPerYear());

        assertFalse(validator.handle(String.format("%s %s %s", transactionType, id, "")));
        assertFalse(validator.handle(String.format("%s %s %s", transactionType, id, "68&(")));
        assertTrue(validator.handle(String.format("%s %s %s", transactionType, id, withdrawAmount)));
    }

    @Test
    protected void withdraw_amounts_to_checking_accounts_should_be_greater_than_0() {
        String id = CHECKING_ID;
        double withdrawAmount = 0;

        assertFalse(validator.handle(String.format("%s %s %s", transactionType, id, withdrawAmount - 200)));
        assertFalse(validator.handle(String.format("%s %s %s", transactionType, id, withdrawAmount - 2)));
        assertFalse(validator.handle(String.format("%s %s %s", transactionType, id, withdrawAmount)));
        assertTrue(validator.handle(String.format("%s %s %s", transactionType, id, withdrawAmount + 2)));
    }

    @Test
    protected void withdraw_amounts_to_checking_accounts_should_be_less_than_or_equal_to_400() {
        String id = CHECKING_ID;
        double withdrawAmount = 400;

        assertTrue(validator.handle(String.format("%s %s %s", transactionType, id, withdrawAmount - 2)));
        assertTrue(validator.handle(String.format("%s %s %s", transactionType, id, withdrawAmount)));
        assertFalse(validator.handle(String.format("%s %s %s", transactionType, id, withdrawAmount + 2)));
        assertFalse(validator.handle(String.format("%s %s %s", transactionType, id, withdrawAmount + 200)));
    }

    @Test
    protected void withdraw_amounts_to_savings_accounts_should_be_greater_than_0() {
        String id = SAVINGS_ID;
        double withdrawAmount = 0;

        assertFalse(validator.handle(String.format("%s %s %s", transactionType, id, withdrawAmount - 200)));
        assertFalse(validator.handle(String.format("%s %s %s", transactionType, id, withdrawAmount - 2)));
        assertFalse(validator.handle(String.format("%s %s %s", transactionType, id, withdrawAmount)));
        assertTrue(validator.handle(String.format("%s %s %s", transactionType, id, withdrawAmount + 2)));
        assertTrue(validator.handle(String.format("%s %s %s", transactionType, id, 500)));
    }

    @Test
    protected void withdraw_amounts_to_savings_accounts_should_be_less_than_or_equal_to_1000() {
        String id = SAVINGS_ID;
        double withdrawAmount = 1000;

        assertTrue(validator.handle(String.format("%s %s %s", transactionType, id, 600)));
        assertTrue(validator.handle(String.format("%s %s %s", transactionType, id, withdrawAmount - 2)));
        assertTrue(validator.handle(String.format("%s %s %s", transactionType, id, withdrawAmount)));
        assertFalse(validator.handle(String.format("%s %s %s", transactionType, id, withdrawAmount + 2)));
        assertFalse(validator.handle(String.format("%s %s %s", transactionType, id, withdrawAmount + 200)));
    }

    @Test
    protected void withdraw_amounts_to_cd_accounts_should_be_greater_than_or_equal_to_the_account_balance() {
        String id = CD_ID;
        double withdrawAmount = timeTravel(startingCDBalance, bank, MONTHS);

        bank.timeTravel(MONTHS);

        assertEquals(withdrawAmount, bank.getAccount(id).getBalance());
        assertFalse(validator.handle(String.format("%s %s %s", transactionType, id, withdrawAmount - 200)));
        assertFalse(validator.handle(String.format("%s %s %s", transactionType, id, withdrawAmount - 2)));
        assertTrue(validator.handle(String.format("%s %s %s", transactionType, id, withdrawAmount)));
        assertTrue(validator.handle(String.format("%s %s %s", transactionType, id, withdrawAmount + 2)));
        assertTrue(validator.handle(String.format("%s %s %s", transactionType, id, withdrawAmount + 200)));

        assertFalse(validator.handle(String.format("%s %s %s", transactionType, id, -1000)));
        assertFalse(validator.handle(String.format("%s %s %s", transactionType, id, 0)));
        assertTrue(validator.handle(String.format("%s %s %s", transactionType, id, Double.POSITIVE_INFINITY)));
    }

    @Test
    protected void withdraw_validators_can_ignore_additional_arguments() {
        bank.timeTravel(MONTHS);

        assertTrue(validator.handle(String.format("%s %s %s %s", transactionType, CHECKING_ID, bank.getAccount(CHECKING_ID).getMaxWithdrawAmount(), "nuke")));
        assertTrue(validator.handle(String.format("%s %s %s %s %s  %s    %s         %s", transactionType, SAVINGS_ID, bank.getAccount(SAVINGS_ID).getMaxWithdrawAmount(), "00", "000", "00000", "000", 0)));
        assertTrue(validator.handle(String.format("%s %s %s %s %s %s", transactionType, CD_ID, bank.getAccount(CD_ID).getMaxWithdrawAmount(), "d", "e", "r")));
    }

    @Test
    protected void withdraw_validators_are_case_insensitive() {
        bank.timeTravel(MONTHS);

        assertTrue(validator.handle(String.format("withdraw %s %s", CHECKING_ID, bank.getAccount(CHECKING_ID).getMaxWithdrawAmount())));
        assertTrue(validator.handle(String.format("wITHdrAw %s %s", SAVINGS_ID, bank.getAccount(SAVINGS_ID).getMaxWithdrawAmount())));
        assertTrue(validator.handle(String.format("WITHDRAW %s %s", CD_ID, bank.getAccount(CD_ID).getMaxWithdrawAmount())));
    }

    @Test
    protected void withdraw_validators_can_be_in_a_chain_of_responsibility() {
        String payingID = CHECKING_ID;
        String receivingID = SAVINGS_ID;
        double transferAmount = min(bank.getAccount(payingID).getMaxWithdrawAmount(), bank.getAccount(receivingID).getMaxDepositAmount());

        validator.setNext(new TransferValidator(bank));

        assertTrue(validator.handle(String.format("%s %s %s %s", TransactionType.Transfer, payingID, receivingID, transferAmount)));
        assertFalse(validator.handle(String.format("%s %s", TransactionType.TimeTravel, MONTHS)));
    }
}
