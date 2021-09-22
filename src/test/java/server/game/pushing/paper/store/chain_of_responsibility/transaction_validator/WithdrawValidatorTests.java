package server.game.pushing.paper.store.chain_of_responsibility.transaction_validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.game.pushing.paper.store.bank.Bank;
import server.game.pushing.paper.store.bank.account.AccountType;
import server.game.pushing.paper.store.chain_of_responsibility.ChainOfResponsibility;
import server.game.pushing.paper.store.chain_of_responsibility.TransactionType;

import static java.lang.Math.min;
import static org.junit.jupiter.api.Assertions.*;
import static server.game.pushing.paper.store.bank.Bank.getMonthsPerYear;
import static server.game.pushing.paper.store.bank.BankTests.passTime;

public class WithdrawValidatorTests {
    private Bank bank;
    private ChainOfResponsibility validator;

    private final int MONTHS = getMonthsPerYear();
    private TransactionType transactionType;
    private final String CHECKING_ID = "34782479";
    private final String SAVINGS_ID = "98430842";
    private final String CD_ID = "43784268";
    private double apr;
    private double initialCDBalance;

    @BeforeEach
    protected void setUp() {
        bank = new Bank();
        validator = new WithdrawValidator(bank);

        transactionType = validator.getTransactionType();
        apr = bank.getMaxAPR();
        initialCDBalance = bank.getMinInitialCDBalance();

        bank.createChecking(CHECKING_ID, apr);
        bank.createSavings(SAVINGS_ID, apr);
        bank.createCD(CD_ID, apr, initialCDBalance);
        bank.deposit(CHECKING_ID, bank.getAccount(CHECKING_ID).getMaxDepositAmount());
        bank.deposit(SAVINGS_ID, bank.getAccount(SAVINGS_ID).getMaxDepositAmount());
    }

    @Test
    protected void withdraw_validator_when_transaction_is_not_valid_should_pass_transaction_down_the_chain_of_responsibility() {
        String fromID = CHECKING_ID;
        String toID = SAVINGS_ID;
        double transferAmount = min(bank.getAccount(fromID).getMaxWithdrawAmount(), bank.getAccount(toID).getMaxDepositAmount());

        validator.setNext(new TransferValidator(bank));

        assertTrue(validator.handle(String.format("%s %s %s %s", TransactionType.Transfer, fromID, toID, transferAmount)));
        assertFalse(validator.handle(String.format("%s %s", TransactionType.PassTime, MONTHS)));
    }

    @Test
    protected void transaction_should_contain_the_transaction_type_withdraw_as_the_first_argument() {
        String id = CHECKING_ID;
        double withdrawAmount = bank.getAccount(id).getMaxWithdrawAmount();

        assertFalse(validator.handle(String.format("%s %s %s", "", "", "")));
        assertFalse(validator.handle(String.format("%s %s %s", "", id, withdrawAmount)));
        assertFalse(validator.handle(String.format("%s %s %s", "nuke", id, withdrawAmount)));
        assertTrue(validator.handle(String.format("%s %s %s", transactionType, id, withdrawAmount)));
    }

    @Test
    protected void transaction_should_contain_a_taken_id_as_the_second_argument() {
        String id = SAVINGS_ID;
        double withdrawAmount = bank.getAccount(id).getMaxWithdrawAmount();

        assertFalse(validator.handle(String.format("%s %s %s", transactionType, "", "")));
        assertFalse(validator.handle(String.format("%s %s %s", transactionType, "", withdrawAmount)));
        assertFalse(validator.handle(String.format("%s %s %s", transactionType, "87439742", withdrawAmount)));
        assertTrue(validator.handle(String.format("%s %s %s", transactionType, id, withdrawAmount)));
    }

    @Test
    protected void transaction_should_contain_a_withdraw_amount_as_the_third_argument() {
        String id = CD_ID;
        double withdrawAmount = bank.getAccount(id).getMaxWithdrawAmount();

        bank.passTime(getMonthsPerYear());

        assertFalse(validator.handle(String.format("%s %s %s", transactionType, id, "")));
        assertFalse(validator.handle(String.format("%s %s %s", transactionType, id, "68&(")));
        assertTrue(validator.handle(String.format("%s %s %s", transactionType, id, withdrawAmount)));
    }

    @Test
    protected void transaction_when_account_type_is_checking_should_contain_a_withdraw_amount_greater_than_0() {
        String id = CHECKING_ID;
        double withdrawAmount = 0;

        assertFalse(validator.handle(String.format("%s %s %s", transactionType, id, withdrawAmount - 200)));
        assertFalse(validator.handle(String.format("%s %s %s", transactionType, id, withdrawAmount - 2)));
        assertFalse(validator.handle(String.format("%s %s %s", transactionType, id, withdrawAmount)));
        assertTrue(validator.handle(String.format("%s %s %s", transactionType, id, withdrawAmount + 2)));
    }

    @Test
    protected void transaction_when_account_type_is_checking_should_contain_a_withdraw_amount_less_than_or_equal_to_400() {
        String id = CHECKING_ID;
        double withdrawAmount = 400;

        assertTrue(validator.handle(String.format("%s %s %s", transactionType, id, withdrawAmount - 2)));
        assertTrue(validator.handle(String.format("%s %s %s", transactionType, id, withdrawAmount)));
        assertFalse(validator.handle(String.format("%s %s %s", transactionType, id, withdrawAmount + 2)));
        assertFalse(validator.handle(String.format("%s %s %s", transactionType, id, withdrawAmount + 200)));
    }

    @Test
    protected void transaction_when_account_type_is_savings_should_not_be_possible_twice_a_month_or_more() {
        String id = SAVINGS_ID;
        double withdrawAmount = bank.getAccount(id).getMaxWithdrawAmount();
        String transaction = String.format("%s %s %s", TransactionType.Withdraw, id, withdrawAmount);

        assertTrue(validator.handle(transaction));
        bank.withdraw(id, withdrawAmount);

        assertFalse(validator.handle(transaction));

        bank.passTime(1);
        assertTrue(validator.handle(transaction));
    }

    @Test
    protected void transaction_when_account_type_is_savings_should_contain_a_withdraw_amount_greater_than_0() {
        String id = SAVINGS_ID;
        double withdrawAmount = 0;

        assertFalse(validator.handle(String.format("%s %s %s", transactionType, id, withdrawAmount - 200)));
        assertFalse(validator.handle(String.format("%s %s %s", transactionType, id, withdrawAmount - 2)));
        assertFalse(validator.handle(String.format("%s %s %s", transactionType, id, withdrawAmount)));
        assertTrue(validator.handle(String.format("%s %s %s", transactionType, id, withdrawAmount + 2)));
        assertTrue(validator.handle(String.format("%s %s %s", transactionType, id, 500)));
    }

    @Test
    protected void transaction_when_account_type_is_savings_should_contain_a_withdraw_amount_less_than_or_equal_to_1000() {
        String id = SAVINGS_ID;
        double withdrawAmount = 1000;

        assertTrue(validator.handle(String.format("%s %s %s", transactionType, id, 600)));
        assertTrue(validator.handle(String.format("%s %s %s", transactionType, id, withdrawAmount - 2)));
        assertTrue(validator.handle(String.format("%s %s %s", transactionType, id, withdrawAmount)));
        assertFalse(validator.handle(String.format("%s %s %s", transactionType, id, withdrawAmount + 2)));
        assertFalse(validator.handle(String.format("%s %s %s", transactionType, id, withdrawAmount + 200)));
    }

    @Test
    protected void transaction_when_account_type_is_cd_should_be_possible_after_a_year_inclusive() {
        int monthsPerYear = getMonthsPerYear();

        for (int month = 0; month < monthsPerYear * 2; month++) {
            assertEquals(month >= monthsPerYear, validator.handle(String.format("%s %s %s", TransactionType.Withdraw, CD_ID, bank.getAccount(CD_ID).getMaxWithdrawAmount())));

            bank.passTime(1);
        }
    }

    @Test
    protected void withdraw_cd_should_be_greater_than_or_equal_to_balance() {
        String id = CD_ID;
        double withdrawAmount = passTime(bank.getMinBalanceFee(), MONTHS, AccountType.CD, apr, initialCDBalance);

        bank.passTime(MONTHS);

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
    protected void transaction_should_be_case_insensitive() {
        bank.passTime(MONTHS);

        assertTrue(validator.handle(String.format("withdraw %s %s", CHECKING_ID, bank.getAccount(CHECKING_ID).getMaxWithdrawAmount())));
        assertTrue(validator.handle(String.format("wITHdrAw %s %s", SAVINGS_ID, bank.getAccount(SAVINGS_ID).getMaxWithdrawAmount())));
        assertTrue(validator.handle(String.format("WITHDRAW %s %s", CD_ID, bank.getAccount(CD_ID).getMaxWithdrawAmount())));
    }

    @Test
    protected void transaction_should_be_possible_with_useless_additional_arguments() {
        bank.passTime(MONTHS);

        assertTrue(validator.handle(String.format("%s %s %s %s", transactionType, CHECKING_ID, bank.getAccount(CHECKING_ID).getMaxWithdrawAmount(), "nuke")));
        assertTrue(validator.handle(String.format("%s %s %s %s %s  %s    %s         %s", transactionType, SAVINGS_ID, bank.getAccount(SAVINGS_ID).getMaxWithdrawAmount(), "00", "000", "00000", "000", 0)));
        assertTrue(validator.handle(String.format("%s %s %s %s %s %s", transactionType, CD_ID, bank.getAccount(CD_ID).getMaxWithdrawAmount(), "d", "e", "r")));
    }
}
