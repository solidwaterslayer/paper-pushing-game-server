package server.game.pushing.paper.ledgervalidator.bank;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.game.pushing.paper.ledgervalidator.bank.account.*;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static server.game.pushing.paper.ledgervalidator.bank.Bank.getMonthsPerYear;

public class AccountTests {
    protected Account checking;
    protected Account savings;
    protected Account cd;

    protected final String CHECKING_ID = "34785729";
    protected final String SAVINGS_ID = "47012479";
    protected final String CD_ID = "34782479";
    protected final double CHECKING_APR = 0.2;
    protected final double SAVINGS_APR = 1;
    protected final double CD_APR = 10;
    protected final double INITIAL_CD_BALANCE = 5835;

    protected double checkingDepositAmount;
    protected double checkingWithdrawAmount;
    protected double savingsDepositAmount;
    protected double savingsWithdrawAmount;
    protected double cdWithdrawAmount;

    @BeforeEach
    protected void setUp() {
        checking = new Checking(CHECKING_ID, CHECKING_APR);
        savings = new Savings(SAVINGS_ID, SAVINGS_APR);
        cd = new CD(CD_ID, CD_APR, INITIAL_CD_BALANCE);
    }

    @Test
    protected void initialize_checking_should_have_0_balance() {
        assertEquals(AccountType.Checking, checking.getAccountType());
        assertEquals(CHECKING_ID, checking.getID());
        assertEquals(CHECKING_APR, checking.getAPR());
        assertEquals(0, checking.getBalance());
    }

    @Test
    protected void initialize_savings_should_have_0_balance() {
        assertEquals(AccountType.Savings, savings.getAccountType());
        assertEquals(SAVINGS_ID, savings.getID());
        assertEquals(SAVINGS_APR, savings.getAPR());
        assertEquals(0, savings.getBalance());
    }

    @Test
    protected void initialize_cd_should_be_possible() {
        assertEquals(AccountType.CD, cd.getAccountType());
        assertEquals(CD_ID, cd.getID());
        assertEquals(CD_APR, cd.getAPR());
        assertEquals(INITIAL_CD_BALANCE, cd.getBalance());
    }

    @Test
    protected void deposit_checking_should_be_possible() {
        checkingDepositAmount = Checking.getMaxDeposit();

        checking.deposit(checkingDepositAmount);

        assertEquals(checkingDepositAmount, checking.getBalance());
    }

    @Test
    protected void deposit_savings_should_be_possible() {
        savingsDepositAmount = Savings.getMaxDeposit();

        savings.deposit(savingsDepositAmount);

        assertEquals(savingsDepositAmount, savings.getBalance());
    }

    @Test
    protected void withdraw_checking_when_less_than_balance_should_be_possible() {
        checkingDepositAmount = Checking.getMaxWithdraw();
        checkingWithdrawAmount = checkingDepositAmount - 100;
        checking.deposit(checkingDepositAmount);

        assertTrue(checkingWithdrawAmount < checking.getBalance());
        checking.withdraw(checkingWithdrawAmount);
        assertEquals(checkingDepositAmount - checkingWithdrawAmount, checking.getBalance());
    }

    @Test
    protected void withdraw_savings_when_less_than_balance_should_be_possible() {
        savingsDepositAmount = Savings.getMaxWithdraw();
        savingsWithdrawAmount = savingsDepositAmount - 50;
        savings.deposit(savingsDepositAmount);

        assertTrue(savingsWithdrawAmount < savings.getBalance());
        savings.withdraw(savingsWithdrawAmount);
        assertEquals(savingsDepositAmount - savingsWithdrawAmount, savings.getBalance());
    }

    @Test
    protected void withdraw_when_equal_to_balance_should_be_possible() {
        int months = getMonthsPerYear();
        checkingDepositAmount = 300;
        savingsDepositAmount = 400;
        checkingWithdrawAmount = checkingDepositAmount;
        savingsWithdrawAmount = savingsDepositAmount;
        cdWithdrawAmount = applyAPR(CD_APR, INITIAL_CD_BALANCE, 4 * months);

        for (int i = 0; i < months; i++) {
            cd.applyAPR();
        }
        depositAndWithdraw();

        assertEquals(0, checking.getBalance());
        assertEquals(0, savings.getBalance());
        assertEquals(0, cd.getBalance());
    }

    @Test
    protected void withdraw_when_greater_than_balance_should_withdraw_amount_equal_to_balance() {
        checkingDepositAmount = 300;
        savingsDepositAmount = 400;
        checkingWithdrawAmount = Checking.getMaxWithdraw();
        savingsWithdrawAmount = Savings.getMaxWithdraw();
        cdWithdrawAmount = CD.getMaxWithdraw();

        for (int i = 0; i < getMonthsPerYear(); i++) {
            cd.applyAPR();
        }
        depositAndWithdraw();

        assertEquals(0, checking.getBalance());
        assertEquals(0, savings.getBalance());
        assertEquals(0, cd.getBalance());
    }

    protected void depositAndWithdraw() {
        checking.deposit(checkingDepositAmount);
        savings.deposit(savingsDepositAmount);
        checking.withdraw(checkingWithdrawAmount);
        savings.withdraw(savingsWithdrawAmount);
        cd.withdraw(cdWithdrawAmount);
    }

    @Test
    protected void apply_apr_checking_should_be_possible() {
        checkingDepositAmount = 200;

        checking.deposit(checkingDepositAmount);
        checking.applyAPR();

        assertEquals(applyAPR(CHECKING_APR, checkingDepositAmount, 1), checking.getBalance());
    }

    @Test
    protected void apply_apr_savings_should_be_possible() {
        savingsDepositAmount = 400;

        savings.deposit(savingsDepositAmount);
        savings.applyAPR();

        assertEquals(applyAPR(SAVINGS_APR, savingsDepositAmount, 1), savings.getBalance());
    }

    @Test
    protected void apply_apr_cd_should_apply_apr_4_times() {
        cd.applyAPR();

        assertEquals(applyAPR(CD_APR, INITIAL_CD_BALANCE, 4), cd.getBalance());
    }

    public static double applyAPR(double apr, double initialBalance, int months) {
        double finalBalance = initialBalance;

        for (int i = 0; i < months; i++) {
            finalBalance += finalBalance * apr / getMonthsPerYear() / 100;
        }

        return finalBalance;
    }

    @Test
    protected void deposit_checking_should_be_greater_than_0() {
        checkingDepositAmount = 0;

        assertFalse(checking.isDepositValid(checkingDepositAmount - 500));
        assertFalse(checking.isDepositValid(checkingDepositAmount - 100));
        assertFalse(checking.isDepositValid(checkingDepositAmount));
        assertTrue(checking.isDepositValid(checkingDepositAmount + 100));
        assertTrue(checking.isDepositValid(500));
    }

    @Test
    protected void deposit_checking_should_be_less_than_or_equal_to_1000() {
        checkingDepositAmount = 1000;

        assertTrue(checking.isDepositValid(600));
        assertTrue(checking.isDepositValid(checkingDepositAmount - 50));
        assertTrue(checking.isDepositValid(checkingDepositAmount));
        assertFalse(checking.isDepositValid(checkingDepositAmount + 50));
        assertFalse(checking.isDepositValid(checkingDepositAmount + 500));
    }

    @Test
    protected void deposit_savings_should_be_greater_than_0() {
        savingsDepositAmount = 0;

        assertFalse(savings.isDepositValid(savingsDepositAmount - 1000));
        assertFalse(savings.isDepositValid(savingsDepositAmount - 50));
        assertFalse(savings.isDepositValid(savingsDepositAmount));
        assertTrue(savings.isDepositValid(savingsDepositAmount + 50));
        assertTrue(savings.isDepositValid(1200));
    }

    @Test
    protected void deposit_savings_should_be_less_than_or_equal_to_2500() {
        savingsDepositAmount = 2500;

        assertTrue(savings.isDepositValid(1300));
        assertTrue(savings.isDepositValid(savingsDepositAmount - 100));
        assertTrue(savings.isDepositValid(savingsDepositAmount));
        assertFalse(savings.isDepositValid(savingsDepositAmount + 100));
        assertFalse(savings.isDepositValid(savingsDepositAmount + 1000));
    }

    @Test
    protected void deposit_cd_should_not_be_possible() {
        List<Double> depositAmounts = Arrays.asList(-500.0, -100.0, 0.0, 100.0, 1200.0, 1300.0, 2400.0, 2500.0, 2600.0, 3500.0);

        for (Double depositAmount : depositAmounts) {
            assertFalse(cd.isDepositValid(depositAmount));
        }
    }

    @Test
    protected void withdraw_checking_should_be_greater_than_0() {
        checkingWithdrawAmount = 0;

        assertFalse(checking.isWithdrawValid(checkingWithdrawAmount - 500));
        assertFalse(checking.isWithdrawValid(checkingWithdrawAmount - 50));
        assertFalse(checking.isWithdrawValid(checkingWithdrawAmount));
        assertTrue(checking.isWithdrawValid(checkingWithdrawAmount + 50));
        assertTrue(checking.isWithdrawValid(100));
    }

    @Test
    protected void withdraw_checking_should_be_less_than_or_equal_to_400() {
        checkingWithdrawAmount = 400;

        assertTrue(checking.isWithdrawValid(200));
        assertTrue(checking.isWithdrawValid(checkingWithdrawAmount - 100));
        assertTrue(checking.isWithdrawValid(checkingWithdrawAmount));
        assertFalse(checking.isWithdrawValid(checkingWithdrawAmount + 100));
        assertFalse(checking.isWithdrawValid(checkingWithdrawAmount + 500));
    }

    @Test
    protected void withdraw_savings_should_be_possible_once_a_month() {
        savingsDepositAmount = Savings.getMaxWithdraw();
        savingsWithdrawAmount = savingsDepositAmount - 100;
        savings.deposit(savingsDepositAmount);

        assertTrue(savings.isWithdrawValid(savingsWithdrawAmount));
        savings.withdraw(savingsWithdrawAmount);

        assertFalse(savings.isWithdrawValid(savingsWithdrawAmount));

        savings.applyAPR();
        assertTrue(savings.isWithdrawValid(savingsWithdrawAmount));
    }

    @Test
    protected void withdraw_savings_should_be_greater_than_0() {
        savingsWithdrawAmount = 0;

        assertFalse(savings.isWithdrawValid(savingsWithdrawAmount - 1000));
        assertFalse(savings.isWithdrawValid(savingsWithdrawAmount - 100));
        assertFalse(savings.isWithdrawValid(savingsWithdrawAmount));
        assertTrue(savings.isWithdrawValid(savingsWithdrawAmount + 100));
        assertTrue(savings.isWithdrawValid(500));
    }

    @Test
    protected void withdraw_savings_should_be_less_than_or_equal_to_1000() {
        savingsWithdrawAmount = 1000;

        assertTrue(savings.isWithdrawValid(600));
        assertTrue(savings.isWithdrawValid(savingsWithdrawAmount - 50));
        assertTrue(savings.isWithdrawValid(savingsWithdrawAmount));
        assertFalse(savings.isWithdrawValid(savingsWithdrawAmount + 50));
        assertFalse(savings.isWithdrawValid(savingsWithdrawAmount + 1000));
    }

    @Test
    protected void withdraw_cd_should_be_possible_after_a_year_inclusive() {
        for (int months = 0; months < getMonthsPerYear() + 12; months++) {
            assertEquals(months >= getMonthsPerYear(), cd.isWithdrawValid(CD.getMaxWithdraw()));

            cd.applyAPR();
        }
    }

    @Test
    protected void withdraw_cd_should_be_greater_than_or_equal_to_balance() {
        int months = getMonthsPerYear();
        cdWithdrawAmount = applyAPR(CD_APR, INITIAL_CD_BALANCE, 4 * months);

        for (int i = 0; i < months; i++) {
            cd.applyAPR();
        }

        assertFalse(cd.isWithdrawValid(-1000));
        assertFalse(cd.isWithdrawValid(0));

        assertEquals(cdWithdrawAmount, cd.getBalance());
        assertFalse(cd.isWithdrawValid(cdWithdrawAmount - 500));
        assertFalse(cd.isWithdrawValid(cdWithdrawAmount - 100));
        assertTrue(cd.isWithdrawValid(cdWithdrawAmount));
        assertTrue(cd.isWithdrawValid(cdWithdrawAmount + 100));
        assertTrue(cd.isWithdrawValid(cdWithdrawAmount + 500));

        assertTrue(cd.isWithdrawValid(Double.POSITIVE_INFINITY));
    }
}
