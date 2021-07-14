package server.game.pushing.paper.bank;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.game.pushing.paper.bank.account.*;

import static org.junit.jupiter.api.Assertions.*;

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
        assertEquals(AccountType.Checking, checking.getAccountType());
        assertEquals(CHECKING_ID, checking.getID());
        assertEquals(CHECKING_APR, checking.getAPR());
        assertEquals(INITIAL_BALANCE, checking.getBalance());
    }

    @Test
    protected void initialize_savings_should_have_0_balance() {
        assertEquals(AccountType.Savings, savings.getAccountType());
        assertEquals(SAVINGS_ID, savings.getID());
        assertEquals(SAVINGS_APR, savings.getAPR());
        assertEquals(INITIAL_BALANCE, savings.getBalance());
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
        checkingDepositAmount = 500;

        checking.deposit(checkingDepositAmount);

        assertEquals(checkingDepositAmount, checking.getBalance());
    }

    @Test
    protected void deposit_savings_should_be_possible() {
        savingsDepositAmount = 300;

        savings.deposit(savingsDepositAmount);

        assertEquals(savingsDepositAmount, savings.getBalance());
    }

    @Test
    protected void withdraw_checking_when_less_than_balance_should_be_possible() {
        checkingDepositAmount = 500;
        checkingWithdrawAmount = 300;

        checking.deposit(checkingDepositAmount);
        checking.withdraw(checkingWithdrawAmount);

        assertEquals(checkingDepositAmount - checkingWithdrawAmount, checking.getBalance());
    }

    @Test
    protected void withdraw_savings_when_less_than_balance_should_be_possible() {
        savingsDepositAmount = 700;
        savingsWithdrawAmount = 400;

        savings.deposit(savingsDepositAmount);
        savings.withdraw(savingsWithdrawAmount);

        assertEquals(savingsDepositAmount - savingsWithdrawAmount, savings.getBalance());
    }

    @Test
    protected void withdraw_when_equal_to_balance_should_be_possible() {
        int months = 12;
        checkingDepositAmount = 300;
        checkingWithdrawAmount = 300;
        savingsDepositAmount = 400;
        savingsWithdrawAmount = 400;
        cdWithdrawAmount = applyAPR(CD_APR, INITIAL_CD_BALANCE, months * 4);

        for (int i = 0; i < months; i++) {
            cd.applyAPR();
        }
        depositAndWithdraw();

        assertEquals(0, checking.getBalance());
        assertEquals(0, savings.getBalance());
        assertEquals(0, cd.getBalance());
    }

    @Test
    protected void withdraw_when_greater_than_balance_should_create_0_balance() {
        checkingDepositAmount = 300;
        checkingWithdrawAmount = 1000;
        savingsDepositAmount = 400;
        savingsWithdrawAmount = 700;
        cdWithdrawAmount = 2000;

        for (int i = 0; i < 12; i++) {
            cd.applyAPR();
        }
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
            finalBalance += finalBalance * apr / 100 / 12;
        }

        return finalBalance;
    }

    @Test
    protected void deposit_checking_should_be_greater_than_0() {
        assertFalse(checking.isDepositValid(-500));

        assertFalse(checking.isDepositValid(-100));
        assertFalse(checking.isDepositValid(0));
        assertTrue(checking.isDepositValid(100));

        assertTrue(checking.isDepositValid(500));
    }

    @Test
    protected void deposit_checking_should_be_less_than_or_equal_to_1000() {
        assertTrue(checking.isDepositValid(600));

        assertTrue(checking.isDepositValid(900));
        assertTrue(checking.isDepositValid(1000));
        assertFalse(checking.isDepositValid(1100));

        assertFalse(checking.isDepositValid(2000));
    }

    @Test
    protected void deposit_savings_should_be_greater_than_0() {
        assertFalse(savings.isDepositValid(-1200));

        assertFalse(savings.isDepositValid(-50));
        assertFalse(savings.isDepositValid(0));
        assertTrue(savings.isDepositValid(50));

        assertTrue(savings.isDepositValid(1200));
    }

    @Test
    protected void deposit_savings_should_be_less_than_or_equal_to_2500() {
        assertTrue(savings.isDepositValid(1300));

        assertTrue(savings.isDepositValid(2400));
        assertTrue(savings.isDepositValid(2500));
        assertFalse(savings.isDepositValid(2600));

        assertFalse(savings.isDepositValid(5000));
    }

    @Test
    protected void deposit_cd_should_not_be_possible() {
        assertFalse(cd.isDepositValid(-1000));

        assertFalse(cd.isDepositValid(-100));
        assertFalse(cd.isDepositValid(0));
        assertFalse(cd.isDepositValid(100));

        assertFalse(cd.isDepositValid(1200));
        assertFalse(cd.isDepositValid(1300));

        assertFalse(cd.isDepositValid(2400));
        assertFalse(cd.isDepositValid(2500));
        assertFalse(cd.isDepositValid(2600));

        assertFalse(cd.isDepositValid(5000));
    }

    @Test
    protected void withdraw_checking_should_be_greater_than_0() {
        assertFalse(checking.isWithdrawValid(-1000));

        assertFalse(checking.isWithdrawValid(-50));
        assertFalse(checking.isWithdrawValid(0));
        assertTrue(checking.isWithdrawValid(50));

        assertTrue(checking.isWithdrawValid(200));
    }

    @Test
    protected void withdraw_checking_should_be_less_than_or_equal_to_400() {
        assertTrue(checking.isWithdrawValid(100));

        assertTrue(checking.isWithdrawValid(300));
        assertTrue(checking.isWithdrawValid(400));
        assertFalse(checking.isWithdrawValid(500));

        assertFalse(checking.isWithdrawValid(1000));
    }

    @Test
    protected void withdraw_savings_twice_a_month_or_more_should_not_be_possible() {
        savingsDepositAmount = 1500;
        savingsWithdrawAmount = 500;

        savings.deposit(savingsDepositAmount);
        assertTrue(savings.isWithdrawValid(savingsWithdrawAmount));

        savings.applyAPR();
        savings.withdraw(savingsWithdrawAmount);
        assertFalse(savings.isWithdrawValid(savingsWithdrawAmount));

        savings.applyAPR();
        savings.withdraw(savingsWithdrawAmount);
        savings.applyAPR();
        assertTrue(savings.isWithdrawValid(savingsWithdrawAmount));
    }

    @Test
    protected void withdraw_savings_should_be_greater_than_0() {
        assertFalse(savings.isWithdrawValid(-500));

        assertFalse(savings.isWithdrawValid(-100));
        assertFalse(savings.isWithdrawValid(0));
        assertTrue(savings.isWithdrawValid(100));

        assertTrue(savings.isWithdrawValid(500));
    }

    @Test
    protected void withdraw_savings_should_be_less_than_or_equal_to_1000() {
        assertTrue(savings.isWithdrawValid(600));

        assertTrue(savings.isWithdrawValid(900));
        assertTrue(savings.isWithdrawValid(1000));
        assertFalse(savings.isWithdrawValid(1100));

        assertFalse(savings.isWithdrawValid(2000));
    }

    @Test
    protected void withdraw_cd_before_12_month_should_not_be_possible() {
        for (int i = 0; i < 24; i++) {
            assertEquals(i >= 12, cd.isWithdrawValid(2000));

            cd.applyAPR();
        }
    }

    @Test
    protected void withdraw_cd_should_be_greater_than_or_equal_to_balance() {
        int months = 12;
        cdWithdrawAmount = applyAPR(CD_APR, INITIAL_CD_BALANCE, months * 4);

        for (int i = 0; i < months; i++) {
            cd.applyAPR();
        }

        assertFalse(cd.isWithdrawValid(cdWithdrawAmount - 500));

        assertFalse(cd.isWithdrawValid(cdWithdrawAmount - 100));
        assertEquals(cdWithdrawAmount, cd.getBalance());
        assertTrue(cd.isWithdrawValid(cdWithdrawAmount));
        assertTrue(cd.isWithdrawValid(cdWithdrawAmount + 100));

        assertTrue(cd.isWithdrawValid(cdWithdrawAmount + 500));
    }
}
