package server.game.pushing.paper.ledgervalidator.transactionchain.transactionvalidator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.game.pushing.paper.ledgervalidator.bank.account.AccountType;
import server.game.pushing.paper.ledgervalidator.bank.Bank;
import server.game.pushing.paper.ledgervalidator.bank.account.CD;
import server.game.pushing.paper.ledgervalidator.bank.account.Checking;
import server.game.pushing.paper.ledgervalidator.bank.account.Savings;
import server.game.pushing.paper.ledgervalidator.transactionchain.TransactionType;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static server.game.pushing.paper.ledgervalidator.bank.Bank.getMaxAPR;
import static server.game.pushing.paper.ledgervalidator.bank.BankTests.passTime;

public class WithdrawValidatorTests {
    protected WithdrawValidator withdrawValidator;
    protected Bank bank;

    protected final String CHECKING_ID = "00000000";
    protected final String SAVINGS_ID = "00000001";
    protected final String CD_ID = "00000010";
    protected final double APR = 0.2;
    protected final double INITIAL_CD_BALANCE = 9000;

    protected final double CD_WITHDRAW_AMOUNT = INITIAL_CD_BALANCE * 2;

    @BeforeEach
    protected void setUp() {
        bank = new Bank(new ArrayList<>(Arrays.asList(
                new Checking(CHECKING_ID, APR),
                new Savings(SAVINGS_ID, APR),
                new CD(CD_ID, APR, INITIAL_CD_BALANCE)
        )));
        bank.deposit(CHECKING_ID, 200);
        bank.deposit(SAVINGS_ID, 1500);

        withdrawValidator = new WithdrawValidator(bank);
    }

    @Test
    protected void withdraw_validator_when_transaction_is_not_valid_should_pass_transaction_up_the_chain_of_responsibility() {
//        depositValidator.setNext(new WithdrawValidator(bank));
//        String checkingID = "87439742";
//        String savingsID = "84382734";
//        double apr = getMaxAPR();
//
//        bank.createChecking(checkingID, apr);
//        bank.createSavings(savingsID, apr);
//
//        assertTrue(depositValidator.handle(String.format("%s %s %s", TransactionType.Withdraw, checkingID, Checking.getMaxWithdrawAmount())));
//        assertFalse(depositValidator.handle(String.format("%s %s %s %s", TransactionType.Transfer, checkingID, savingsID, 400)));
        withdrawValidator.setNext(new TransferValidator(bank));
        bank.createSavings("00000000", 0);
        bank.createChecking("00000001", 0);

        assertTrue(withdrawValidator.handle("transfer 00000000 00000001 1000"));
        assertFalse(withdrawValidator.handle("pass time 60"));
    }

    @Test
    protected void transaction_should_contain_the_transaction_type_withdraw_as_the_first_argument() {
        assertFalse(withdrawValidator.handle(""));
        assertFalse(withdrawValidator.handle(String.format(" %s 400", CHECKING_ID)));
        assertFalse(withdrawValidator.handle(String.format("nuke %s 400", CHECKING_ID)));
        assertTrue(withdrawValidator.handle(String.format("withdraw %s 400", CHECKING_ID)));
    }

    @Test
    protected void transaction_should_contain_a_taken_id_as_the_second_argument() {
        assertFalse(withdrawValidator.handle("withdraw 47386825 1000"));

        assertFalse(withdrawValidator.handle("withdraw"));
        assertFalse(withdrawValidator.handle("withdraw  1000"));

        assertTrue(withdrawValidator.handle(String.format("withdraw %s 1000", SAVINGS_ID)));
    }

    @Test
    protected void transaction_should_contain_a_withdraw_amount_as_the_third_argument() {
        bank.passTime(12);

        assertFalse(withdrawValidator.handle(String.format("withdraw %s 7g8Y&*", CD_ID)));
        assertFalse(withdrawValidator.handle(String.format("withdraw %s", CD_ID)));
        assertTrue(withdrawValidator.handle(String.format("withdraw %s %f", CD_ID, CD_WITHDRAW_AMOUNT)));
    }

    @Test
    protected void transaction_when_account_type_is_checking_should_contain_a_withdraw_amount_greater_than_0() {
        assertFalse(withdrawValidator.handle(String.format("withdraw %s -1000", CHECKING_ID)));

        assertFalse(withdrawValidator.handle(String.format("withdraw %s -50", CHECKING_ID)));
        assertFalse(withdrawValidator.handle(String.format("withdraw %s 0", CHECKING_ID)));
        assertTrue(withdrawValidator.handle(String.format("withdraw %s 50", CHECKING_ID)));

        assertTrue(withdrawValidator.handle(String.format("withdraw %s 200", CHECKING_ID)));
    }

    @Test
    protected void transaction_when_account_type_is_checking_should_contain_a_withdraw_amount_less_than_or_equal_to_400() {
        assertTrue(withdrawValidator.handle(String.format("withdraw %s 100", CHECKING_ID)));

        assertTrue(withdrawValidator.handle(String.format("withdraw %s 300", CHECKING_ID)));
        assertTrue(withdrawValidator.handle(String.format("withdraw %s 400", CHECKING_ID)));
        assertFalse(withdrawValidator.handle(String.format("withdraw %s 500", CHECKING_ID)));

        assertFalse(withdrawValidator.handle(String.format("withdraw %s 1000", CHECKING_ID)));
    }

    @Test
    protected void transaction_when_account_type_is_savings_should_be_possible_once_a_month() {
        double savingsWithdrawAmount = 500;

        assertTrue(withdrawValidator.handle(String.format("withdraw %s %f", SAVINGS_ID, savingsWithdrawAmount)));

        bank.passTime(1);
        bank.withdraw(SAVINGS_ID, savingsWithdrawAmount);
        assertFalse(withdrawValidator.handle(String.format("withdraw %s %f", SAVINGS_ID, savingsWithdrawAmount)));

        bank.withdraw(SAVINGS_ID, savingsWithdrawAmount);
        bank.passTime(1);
        assertTrue(withdrawValidator.handle(String.format("withdraw %s %f", SAVINGS_ID, savingsWithdrawAmount)));
    }

    @Test
    protected void transaction_when_account_type_is_savings_should_contain_a_withdraw_amount_greater_than_0() {
        assertFalse(withdrawValidator.handle(String.format("withdraw %s -1000", SAVINGS_ID)));

        assertFalse(withdrawValidator.handle(String.format("withdraw %s -50", SAVINGS_ID)));
        assertFalse(withdrawValidator.handle(String.format("withdraw %s 0", SAVINGS_ID)));
        assertTrue(withdrawValidator.handle(String.format("withdraw %s 50", SAVINGS_ID)));

        assertTrue(withdrawValidator.handle(String.format("withdraw %s 500", SAVINGS_ID)));
    }

    @Test
    protected void transaction_when_account_type_is_savings_should_contain_a_withdraw_amount_less_than_or_equal_to_1000() {
        assertTrue(withdrawValidator.handle(String.format("withdraw %s 600", SAVINGS_ID)));

        assertTrue(withdrawValidator.handle(String.format("withdraw %s 900", SAVINGS_ID)));
        assertTrue(withdrawValidator.handle(String.format("withdraw %s 1000", SAVINGS_ID)));
        assertFalse(withdrawValidator.handle(String.format("withdraw %s 1100", SAVINGS_ID)));

        assertFalse(withdrawValidator.handle(String.format("withdraw %s 2000", SAVINGS_ID)));
    }

    @Test
    protected void withdraw_cd_before_12_month_should_be_possible() {
        for (int i = 0; i < 24; i++) {
            assertEquals(i >= 12, withdrawValidator.handle(String.format("withdraw %s %f", CD_ID, CD_WITHDRAW_AMOUNT)));

            bank.passTime(1);
        }
    }

    @Test
    protected void withdraw_cd_should_be_greater_than_or_equal_to_balance() {
        int months = 12;
        double cdWithdrawAmount = passTime(APR, bank.getMinBalanceFee(), AccountType.CD, INITIAL_CD_BALANCE, months);

        bank.passTime(months);

        assertFalse(withdrawValidator.handle(String.format("withdraw %s %f", CD_ID, cdWithdrawAmount - 500)));
        assertFalse(withdrawValidator.handle(String.format("withdraw %s %f", CD_ID, cdWithdrawAmount - 100)));
        assertEquals(cdWithdrawAmount, bank.getAccount(CD_ID).getBalance());
        assertTrue(withdrawValidator.handle(String.format("withdraw %s %.20f", CD_ID, cdWithdrawAmount)));
        assertTrue(withdrawValidator.handle(String.format("withdraw %s %f", CD_ID, cdWithdrawAmount + 100)));
        assertTrue(withdrawValidator.handle(String.format("withdraw %s %f", CD_ID, cdWithdrawAmount + 500)));
    }

    @Test
    protected void transaction_should_be_case_insensitive() {
        bank.passTime(12);
        assertTrue(withdrawValidator.handle(String.format("wiThdRAw %s %f", CHECKING_ID, 400.0f)));
        assertTrue(withdrawValidator.handle(String.format("wITHdrAw %s %f", SAVINGS_ID, 1000.0f)));
        assertTrue(withdrawValidator.handle(String.format("WITHDRAW %s %f", CD_ID, CD_WITHDRAW_AMOUNT)));
    }

    @Test
    protected void transaction_should_be_possible_with_useless_additional_arguments() {
        bank.passTime(12);
        assertTrue(withdrawValidator.handle(String.format("withdraw %s %f nuke", CHECKING_ID, 400.0f)));
        assertTrue(withdrawValidator.handle(String.format("withdraw %s %f 0 0 0 0 0 0", SAVINGS_ID, 1000.0f)));
        assertTrue(withdrawValidator.handle(String.format("withdraw %s %f 0 0    0 0  0 0  0 0    0 0 0 0 0  0 0   0 0 0 0", CD_ID, CD_WITHDRAW_AMOUNT)));
    }
}
