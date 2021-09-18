package server.game.pushing.paper.ledgervalidator.transactionchain.transactionvalidator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.game.pushing.paper.ledgervalidator.bank.Bank;
import server.game.pushing.paper.ledgervalidator.bank.account.AccountType;
import server.game.pushing.paper.ledgervalidator.transactionchain.TransactionType;

import static org.junit.jupiter.api.Assertions.*;
import static server.game.pushing.paper.ledgervalidator.bank.Bank.getMonthsPerYear;
import static server.game.pushing.paper.ledgervalidator.bank.BankTests.passTime;

public class WithdrawValidatorTests {
    protected Bank bank;
    protected WithdrawValidator withdrawValidator;

    protected final String CHECKING_ID = "34782479";
    protected final String SAVINGS_ID = "98430842";
    protected final String CD_ID = "43784268";
    protected double apr;
    protected double initialCDBalance;

    @BeforeEach
    protected void setUp() {
        bank = new Bank();
        withdrawValidator = new WithdrawValidator(bank);

        apr = bank.getMaxAPR();
        initialCDBalance = bank.getMinInitialCDBalance();

        bank.createChecking(CHECKING_ID, apr);
        bank.createSavings(SAVINGS_ID, apr);
        bank.createCD(CD_ID, apr, initialCDBalance);
        bank.deposit(CHECKING_ID, bank.getAccount(CHECKING_ID).getMaxDepositAmount());
        bank.deposit(SAVINGS_ID, bank.getAccount(SAVINGS_ID).getMaxDepositAmount());
    }

    @Test
    protected void withdraw_validator_when_transaction_is_not_valid_should_pass_transaction_up_the_chain_of_responsibility() {
        double transferAmount = 400;
        int months = getMonthsPerYear();

        withdrawValidator.setNext(new TransferValidator(bank));

        assertTrue(withdrawValidator.handle(String.format("%s %s %s %s", TransactionType.Transfer, CHECKING_ID, SAVINGS_ID, transferAmount)));
        assertFalse(withdrawValidator.handle(String.format("%s %s", TransactionType.PassTime, months)));
    }

    @Test
    protected void transaction_should_contain_the_transaction_type_withdraw_as_the_first_argument() {
        TransactionType transactionType = TransactionType.Withdraw;
        String id = CHECKING_ID;
        double withdrawAmount = bank.getAccount(id).getMaxWithdrawAmount();

        assertFalse(withdrawValidator.handle(String.format("%s %s %s", "", "", "")));
        assertFalse(withdrawValidator.handle(String.format("%s %s %s", "", id, withdrawAmount)));
        assertFalse(withdrawValidator.handle(String.format("%s %s %s", "nuke", id, withdrawAmount)));
        assertTrue(withdrawValidator.handle(String.format("%s %s %s", transactionType, id, withdrawAmount)));
    }

    @Test
    protected void transaction_should_contain_a_taken_id_as_the_second_argument() {
        TransactionType transactionType = TransactionType.Withdraw;
        String id = SAVINGS_ID;
        double withdrawAmount = bank.getAccount(id).getMaxWithdrawAmount();

        assertFalse(withdrawValidator.handle(String.format("%s %s %s", transactionType, "", "")));
        assertFalse(withdrawValidator.handle(String.format("%s %s %s", transactionType, "", withdrawAmount)));
        assertFalse(withdrawValidator.handle(String.format("%s %s %s", transactionType, "87439742", withdrawAmount)));
        assertTrue(withdrawValidator.handle(String.format("%s %s %s", transactionType, id, withdrawAmount)));
    }

    @Test
    protected void transaction_should_contain_a_withdraw_amount_as_the_third_argument() {
        TransactionType transactionType = TransactionType.Withdraw;
        String id = CD_ID;
        double withdrawAmount = bank.getAccount(id).getMaxWithdrawAmount();

        bank.passTime(getMonthsPerYear());

        assertFalse(withdrawValidator.handle(String.format("%s %s %s", transactionType, id, "")));
        assertFalse(withdrawValidator.handle(String.format("%s %s %s", transactionType, id, "68&(")));
        assertTrue(withdrawValidator.handle(String.format("%s %s %s", transactionType, id, withdrawAmount)));
    }

    @Test
    protected void transaction_when_account_type_is_checking_should_contain_a_withdraw_amount_greater_than_0() {
        TransactionType transactionType = TransactionType.Withdraw;
        String id = CHECKING_ID;
        double withdrawAmount = 0;

        assertFalse(withdrawValidator.handle(String.format("%s %s %s", transactionType, id, withdrawAmount - 200)));
        assertFalse(withdrawValidator.handle(String.format("%s %s %s", transactionType, id, withdrawAmount - 2)));
        assertFalse(withdrawValidator.handle(String.format("%s %s %s", transactionType, id, withdrawAmount)));
        assertTrue(withdrawValidator.handle(String.format("%s %s %s", transactionType, id, withdrawAmount + 2)));
    }

    @Test
    protected void transaction_when_account_type_is_checking_should_contain_a_withdraw_amount_less_than_or_equal_to_400() {
        TransactionType transactionType = TransactionType.Withdraw;
        String id = CHECKING_ID;
        double withdrawAmount = 400;

        assertTrue(withdrawValidator.handle(String.format("%s %s %s", transactionType, id, withdrawAmount - 2)));
        assertTrue(withdrawValidator.handle(String.format("%s %s %s", transactionType, id, withdrawAmount)));
        assertFalse(withdrawValidator.handle(String.format("%s %s %s", transactionType, id, withdrawAmount + 2)));
        assertFalse(withdrawValidator.handle(String.format("%s %s %s", transactionType, id, withdrawAmount + 200)));
    }

    @Test
    protected void transaction_when_account_type_is_savings_should_not_be_possible_twice_a_month_or_more() {
        String id = SAVINGS_ID;
        double withdrawAmount = bank.getAccount(id).getMaxWithdrawAmount();
        String transaction = String.format("%s %s %s", TransactionType.Withdraw, id, withdrawAmount);

        assertTrue(withdrawValidator.handle(transaction));
        bank.withdraw(id, withdrawAmount);

        assertFalse(withdrawValidator.handle(transaction));

        bank.passTime(1);
        assertTrue(withdrawValidator.handle(transaction));
    }

    @Test
    protected void transaction_when_account_type_is_savings_should_contain_a_withdraw_amount_greater_than_0() {
        TransactionType transactionType = TransactionType.Withdraw;
        String id = SAVINGS_ID;
        double withdrawAmount = 0;

        assertFalse(withdrawValidator.handle(String.format("%s %s %s", transactionType, id, withdrawAmount - 200)));
        assertFalse(withdrawValidator.handle(String.format("%s %s %s", transactionType, id, withdrawAmount - 2)));
        assertFalse(withdrawValidator.handle(String.format("%s %s %s", transactionType, id, withdrawAmount)));
        assertTrue(withdrawValidator.handle(String.format("%s %s %s", transactionType, id, withdrawAmount + 2)));
        assertTrue(withdrawValidator.handle(String.format("%s %s %s", transactionType, id, 500)));
    }

    @Test
    protected void transaction_when_account_type_is_savings_should_contain_a_withdraw_amount_less_than_or_equal_to_1000() {
        TransactionType transactionType = TransactionType.Withdraw;
        String id = SAVINGS_ID;
        double withdrawAmount = 1000;

        assertTrue(withdrawValidator.handle(String.format("%s %s %s", transactionType, id, 600)));
        assertTrue(withdrawValidator.handle(String.format("%s %s %s", transactionType, id, withdrawAmount - 2)));
        assertTrue(withdrawValidator.handle(String.format("%s %s %s", transactionType, id, withdrawAmount)));
        assertFalse(withdrawValidator.handle(String.format("%s %s %s", transactionType, id, withdrawAmount + 2)));
        assertFalse(withdrawValidator.handle(String.format("%s %s %s", transactionType, id, withdrawAmount + 200)));
    }

    @Test
    protected void transaction_when_account_type_is_cd_should_be_possible_after_a_year_inclusive() {
        int monthsPerYear = getMonthsPerYear();

        for (int month = 0; month < monthsPerYear * 2; month++) {
            assertEquals(month >= monthsPerYear, withdrawValidator.handle(String.format("%s %s %s", TransactionType.Withdraw, CD_ID, bank.getAccount(CD_ID).getMaxWithdrawAmount())));

            bank.passTime(1);
        }
    }

    @Test
    protected void withdraw_cd_should_be_greater_than_or_equal_to_balance() {
        int months = getMonthsPerYear();
        TransactionType transactionType = TransactionType.Withdraw;
        String id = CD_ID;
        double withdrawAmount = passTime(bank.getMinBalanceFee(), months, AccountType.CD, apr, initialCDBalance);

        bank.passTime(months);

        assertEquals(withdrawAmount, bank.getAccount(CD_ID).getBalance());
        assertFalse(withdrawValidator.handle(String.format("%s %s %s", transactionType, id, withdrawAmount - 200)));
        assertFalse(withdrawValidator.handle(String.format("%s %s %s", transactionType, id, withdrawAmount - 2)));
        assertTrue(withdrawValidator.handle(String.format("%s %s %s", transactionType, id, withdrawAmount)));
        assertTrue(withdrawValidator.handle(String.format("%s %s %s", transactionType, id, withdrawAmount + 2)));
        assertTrue(withdrawValidator.handle(String.format("%s %s %s", transactionType, id, withdrawAmount + 200)));

        assertFalse(withdrawValidator.handle(String.format("%s %s %s", transactionType, id, -1000)));
        assertFalse(withdrawValidator.handle(String.format("%s %s %s", transactionType, id, 0)));
        assertTrue(withdrawValidator.handle(String.format("%s %s %s", transactionType, id, Double.POSITIVE_INFINITY)));
    }

    @Test
    protected void transaction_should_be_case_insensitive() {
        bank.passTime(getMonthsPerYear());

        assertTrue(withdrawValidator.handle(String.format("withdraw %s %s", CHECKING_ID, bank.getAccount(CHECKING_ID).getMaxWithdrawAmount())));
        assertTrue(withdrawValidator.handle(String.format("wITHdrAw %s %s", SAVINGS_ID, bank.getAccount(SAVINGS_ID).getMaxWithdrawAmount())));
        assertTrue(withdrawValidator.handle(String.format("WITHDRAW %s %s", CD_ID, bank.getAccount(CD_ID).getMaxWithdrawAmount())));
    }

    @Test
    protected void transaction_should_be_possible_with_useless_additional_arguments() {
        TransactionType transactionType = TransactionType.Withdraw;

        bank.passTime(getMonthsPerYear());

        assertTrue(withdrawValidator.handle(String.format("%s %s %s %s", transactionType, CHECKING_ID, bank.getAccount(CHECKING_ID).getMaxWithdrawAmount(), "nuke")));
        assertTrue(withdrawValidator.handle(String.format("%s %s %s %s %s  %s    %s         %s", transactionType, SAVINGS_ID, bank.getAccount(SAVINGS_ID).getMaxWithdrawAmount(), "00", "000", "00000", "000", 0)));
        assertTrue(withdrawValidator.handle(String.format("%s %s %s %s %s %s", transactionType, CD_ID, bank.getAccount(CD_ID).getMaxWithdrawAmount(), "d", "e", "r")));
    }
}
