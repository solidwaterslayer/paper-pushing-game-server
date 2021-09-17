package server.game.pushing.paper.ledgervalidator.transactionchain.transactionvalidator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.game.pushing.paper.ledgervalidator.bank.Bank;
import server.game.pushing.paper.ledgervalidator.bank.account.CD;
import server.game.pushing.paper.ledgervalidator.bank.account.Checking;
import server.game.pushing.paper.ledgervalidator.bank.account.Savings;
import server.game.pushing.paper.ledgervalidator.transactionchain.TransactionType;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static server.game.pushing.paper.ledgervalidator.bank.Bank.getMaxAPR;
import static server.game.pushing.paper.ledgervalidator.bank.Bank.getMinInitialCDBalance;

public class DepositValidatorTests {
    protected Bank bank;
    protected DepositValidator depositValidator;

    protected final String CHECKING_ID = "09096564";
    protected final String SAVINGS_ID = "90438954";
    protected final String CD_ID = "98430842";
    protected final double APR = getMaxAPR();
    protected final double INITIAL_CD_BALANCE = getMinInitialCDBalance();

    @BeforeEach
    protected void setUp() {
        bank = new Bank(Arrays.asList(
                new Checking(CHECKING_ID, APR),
                new Savings(SAVINGS_ID, APR),
                new CD(CD_ID, APR, INITIAL_CD_BALANCE)
        ));
        depositValidator = new DepositValidator(bank);
    }

    @Test
    protected void deposit_validator_when_transaction_is_not_valid_should_pass_transaction_up_the_chain_of_responsibility() {
        String id = CHECKING_ID;
        double withdrawAmount = bank.getAccount(id).getMaxWithdrawAmount();
        double transferAmount = 400;

        depositValidator.setNext(new WithdrawValidator(bank));

        assertTrue(depositValidator.handle(String.format("%s %s %s", TransactionType.Withdraw, id, withdrawAmount)));
        assertFalse(depositValidator.handle(String.format("%s %s %s %s", TransactionType.Transfer, id, SAVINGS_ID, transferAmount)));
    }

    @Test
    protected void transaction_should_contain_the_transaction_type_deposit_as_the_first_argument() {
        TransactionType transactionType = TransactionType.Deposit;
        String id = SAVINGS_ID;
        double depositAmount = bank.getAccount(id).getMaxDepositAmount();

        assertFalse(depositValidator.handle(String.format("%s %s %s", "", "", "")));
        assertFalse(depositValidator.handle(String.format("%s %s %s", "", id, depositAmount)));
        assertFalse(depositValidator.handle(String.format("%s %s %s", "nuke", id, depositAmount)));
        assertTrue(depositValidator.handle(String.format("%s %s %s", transactionType, id, depositAmount)));
    }

    @Test
    protected void transaction_should_contain_a_taken_id_as_the_second_argument() {
        TransactionType transactionType = TransactionType.Deposit;
        String id = SAVINGS_ID;
        double depositAmount = bank.getAccount(id).getMaxDepositAmount();

        assertFalse(depositValidator.handle(String.format("%s %s %s", transactionType, "", "")));
        assertFalse(depositValidator.handle(String.format("%s %s %s", transactionType, "", depositAmount)));
        assertFalse(depositValidator.handle(String.format("%s %s %s", transactionType, "87439742", depositAmount)));
        assertTrue(depositValidator.handle(String.format("%s %s %s", transactionType, id, depositAmount)));
    }

    @Test
    protected void transaction_should_contain_a_deposit_amount_as_the_third_argument() {
        TransactionType transactionType = TransactionType.Deposit;
        String id = SAVINGS_ID;
        double depositAmount = bank.getAccount(id).getMaxDepositAmount();

        assertFalse(depositValidator.handle(String.format("%s %s %s", transactionType, id, "")));
        assertFalse(depositValidator.handle(String.format("%s %s %s", transactionType, id, "depositAmount")));
        assertTrue(depositValidator.handle(String.format("%s %s %s", transactionType, id, depositAmount)));
    }

    @Test
    protected void transaction_when_account_type_is_checking_should_contain_a_deposit_amount_greater_than_0() {
        TransactionType transactionType = TransactionType.Deposit;
        String id = CHECKING_ID;
        double depositAmount = 0;

        assertFalse(depositValidator.handle(String.format("%s %s %s", transactionType, id, depositAmount - 1000)));
        assertFalse(depositValidator.handle(String.format("%s %s %s", transactionType, id, depositAmount - 1)));
        assertFalse(depositValidator.handle(String.format("%s %s %s", transactionType, id, depositAmount)));
        assertTrue(depositValidator.handle(String.format("%s %s %s", transactionType, id, depositAmount + 1)));
        assertTrue(depositValidator.handle(String.format("%s %s %s", transactionType, id, 500)));
    }

    @Test
    protected void transaction_when_account_type_is_checking_should_contain_a_deposit_amount_less_than_or_equal_to_1000() {
        TransactionType transactionType = TransactionType.Deposit;
        String id = CHECKING_ID;
        double depositAmount = 1000;

        assertTrue(depositValidator.handle(String.format("%s %s %s", transactionType, id, 600)));
        assertTrue(depositValidator.handle(String.format("%s %s %s", transactionType, id, depositAmount - 1)));
        assertTrue(depositValidator.handle(String.format("%s %s %s", transactionType, id, depositAmount)));
        assertFalse(depositValidator.handle(String.format("%s %s %s", transactionType, id, depositAmount + 1)));
        assertFalse(depositValidator.handle(String.format("%s %s %s", transactionType, id, depositAmount + 1000)));
    }

    @Test
    protected void transaction_when_account_type_is_savings_should_contain_a_deposit_amount_greater_than_0() {
        TransactionType transactionType = TransactionType.Deposit;
        String id = SAVINGS_ID;
        double depositAmount = 0;

        assertFalse(depositValidator.handle(String.format("%s %s %s", transactionType, id, depositAmount - 1000)));
        assertFalse(depositValidator.handle(String.format("%s %s %s", transactionType, id, depositAmount - 1)));
        assertFalse(depositValidator.handle(String.format("%s %s %s", transactionType, id, depositAmount)));
        assertTrue(depositValidator.handle(String.format("%s %s %s", transactionType, id, depositAmount + 1)));
        assertTrue(depositValidator.handle(String.format("%s %s %s", transactionType, id, 500)));
    }

    @Test
    protected void transaction_when_account_type_is_savings_should_contain_a_deposit_amount_less_than_or_equal_to_2500() {
        TransactionType transactionType = TransactionType.Deposit;
        String id = SAVINGS_ID;
        double depositAmount = 2500;

        assertTrue(depositValidator.handle(String.format("%s %s %s", transactionType, id, 600)));
        assertTrue(depositValidator.handle(String.format("%s %s %s", transactionType, id, depositAmount - 1)));
        assertTrue(depositValidator.handle(String.format("%s %s %s", transactionType, id, depositAmount)));
        assertFalse(depositValidator.handle(String.format("%s %s %s", transactionType, id, depositAmount + 1)));
        assertFalse(depositValidator.handle(String.format("%s %s %s", transactionType, id, depositAmount + 1000)));
    }

    @Test
    protected void transaction_when_account_type_is_cd_should_not_be_possible() {
        TransactionType transactionType = TransactionType.Deposit;
        List<Double> depositAmounts = Arrays.asList(-1000.0, -1.0, 0.0, 1.0, 1200.0, 1300.0, 2400.0, 2500.0, 2501.0, 3500.0);

        for (Double depositAmount : depositAmounts) {
            assertFalse(depositValidator.handle(String.format("%s %s %s", transactionType, CD_ID, depositAmount)));
        }
    }

    @Test
    protected void transaction_should_be_case_insensitive() {
        assertTrue(depositValidator.handle(String.format("dePoSIT %s %s", CHECKING_ID, bank.getAccount(CHECKING_ID).getMaxDepositAmount())));
        assertTrue(depositValidator.handle(String.format("deposit %s %s", SAVINGS_ID, bank.getAccount(SAVINGS_ID).getMaxDepositAmount())));
    }

    @Test
    protected void transaction_should_be_possible_with_useless_additional_arguments() {
        TransactionType transactionType = TransactionType.Deposit;

        assertTrue(depositValidator.handle(String.format("%s %s %s nuke", transactionType, CHECKING_ID, bank.getAccount(CHECKING_ID).getMaxDepositAmount())));
        assertTrue(depositValidator.handle(String.format("%s %s %s 0 0 0 0 0 0  0 0", transactionType, SAVINGS_ID, bank.getAccount(SAVINGS_ID).getMaxDepositAmount())));
    }
}
