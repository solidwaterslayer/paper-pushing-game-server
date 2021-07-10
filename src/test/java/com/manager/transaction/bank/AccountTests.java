package com.manager.transaction.bank;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AccountTests {
    protected final String CHECKING_ID = "00000000";
    protected final String SAVINGS_ID = "00000001";
    protected final String CD_ID = "00000010";

    protected final double CHECKING_APR = 0.2;
    protected final double SAVINGS_APR = 0.8;
    protected final double CD_APR = 0.4;

    protected final double INITIAL_BALANCE = 0;
    protected final double INITIAL_CD_BALANCE = 1000;

    protected Account checking;
    protected Account savings;
    protected Account cd;

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
        assertEquals(CHECKING_ID, checking.getID());
        assertEquals(CHECKING_APR, checking.getAPR());
        assertEquals(INITIAL_BALANCE, checking.getBalance());
    }

    @Test
    protected void initialize_savings_should_have_0_balance() {
        assertEquals(SAVINGS_ID, savings.getID());
        assertEquals(SAVINGS_APR, savings.getAPR());
        assertEquals(INITIAL_BALANCE, savings.getBalance());
    }

    @Test
    protected void initialize_cd_should_be_possible() {
        assertEquals(CD_ID, cd.getID());
        assertEquals(CD_APR, cd.getAPR());
        assertEquals(INITIAL_CD_BALANCE, cd.getBalance());
    }

    @Test
    protected void deposit_checking_should_be_possible() {
        checkingDepositAmount = 500;

        checking.deposit(checkingDepositAmount);

        assertEquals(checkingDepositAmount, checking.getBalance());
    }

    @Test
    protected void deposit_savings_should_be_possible() {
        savingsWithdrawAmount = 300;

        savings.deposit(savingsWithdrawAmount);

        assertEquals(savingsWithdrawAmount, savings.getBalance());
    }

    @Test
    protected void withdraw_when_less_than_balance_should_be_possible() {
        checkingDepositAmount = 500;
        checkingWithdrawAmount = 300;
        savingsDepositAmount = 700;
        savingsWithdrawAmount = 400;
        cdWithdrawAmount = 500;

        depositAndWithdraw();

        assertEquals(checkingDepositAmount - checkingWithdrawAmount, checking.getBalance());
        assertEquals(savingsDepositAmount - savingsWithdrawAmount, savings.getBalance());
        assertEquals(INITIAL_CD_BALANCE - cdWithdrawAmount, cd.getBalance());
    }

    @Test
    protected void withdraw_when_equal_to_balance_should_be_possible() {
        checkingDepositAmount = 300;
        checkingWithdrawAmount = 300;
        savingsDepositAmount = 400;
        savingsWithdrawAmount = 400;
        cdWithdrawAmount = 1000;

        depositAndWithdraw();

        assertEquals(checkingDepositAmount - checkingWithdrawAmount, checking.getBalance());
        assertEquals(savingsDepositAmount - savingsWithdrawAmount, savings.getBalance());
        assertEquals(INITIAL_CD_BALANCE - cdWithdrawAmount, cd.getBalance());
    }

    @Test
    protected void withdraw_when_greater_than_balance_should_leave_0_balance() {
        checkingDepositAmount = 300;
        checkingWithdrawAmount = 1000;
        savingsDepositAmount = 400;
        savingsWithdrawAmount = 700;
        cdWithdrawAmount = 2000;

        depositAndWithdraw();

        assertEquals(0, checking.getBalance());
        assertEquals(0, savings.getBalance());
        assertEquals(0, cd.getBalance());
    }

    protected void depositAndWithdraw() {
        checking.deposit(checkingDepositAmount);
        checking.withdraw(checkingWithdrawAmount);
        savings.deposit(savingsDepositAmount);
        savings.withdraw(savingsWithdrawAmount);
        cd.withdraw(cdWithdrawAmount);
    }

    @Test
    void apply_apr_checking_should_be_possible() {
        checkingDepositAmount = 200;

        checking.deposit(checkingDepositAmount);
        checking.applyAPR();

        assertEquals(applyAPR(CHECKING_APR, checkingDepositAmount, 1), checking.getBalance());
    }

    @Test
    void apply_apr_savings_should_be_possible() {
        savingsDepositAmount = 400;

        savings.deposit(savingsDepositAmount);
        savings.applyAPR();

        assertEquals(applyAPR(SAVINGS_APR, savingsDepositAmount, 1), savings.getBalance());
    }

    @Test
    void apply_apr_cd_should_apply_apr_4_times() {
        cd.applyAPR();

        assertEquals(applyAPR(CD_APR, INITIAL_CD_BALANCE, 4), cd.getBalance());
    }

    public double applyAPR(double apr, double initialBalance, int iterations) {
        double finalBalance = initialBalance;

        for (int i = 0; i < iterations; i++) {
            finalBalance += finalBalance * apr / 100 / 12;
        }

        return finalBalance;
    }

    @Test
    void apply_min_balance_fee_should_be_possible() {
        savings.deposit(100);
        savings.applyMinBalanceFee();

        assertEquals(100 - 25, savings.getBalance());
    }
}
