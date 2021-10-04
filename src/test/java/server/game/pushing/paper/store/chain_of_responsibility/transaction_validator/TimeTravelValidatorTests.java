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

public class TimeTravelValidatorTests {
    private Bank bank;
    private ChainOfResponsibility validator;

    private final int MONTHS_PER_YEAR = getMonthsPerYear();
    private TransactionType transactionType;
    private final String CHECKING_ID = "98430843";
    private final String SAVINGS_ID = "98430842";
    private final String CD_ID = "87439742";
    private double apr;

    @BeforeEach
    protected void setUp() {
        bank = new Bank();
        validator = new TimeTravelValidator(bank);

        transactionType = validator.getTransactionType();
        apr = bank.getMaxAPR();
        double initialCDBalance = bank.getMinInitialCDBalance();

        bank.createChecking(CHECKING_ID, apr);
        bank.createSavings(SAVINGS_ID, apr);
        bank.createCD(CD_ID, apr, initialCDBalance);
    }

    @Test
    protected void the_first_and_second_argument_of_time_travel_transaction_is_the_transaction_type_time_travel() {
        assertFalse(validator.handle(""));
        assertFalse(validator.handle(String.format("%s %s", "", "")));
        assertFalse(validator.handle(String.format("%s %s", "", MONTHS_PER_YEAR)));
        assertFalse(validator.handle(String.format("%s %s", "yes no", MONTHS_PER_YEAR)));
        assertTrue(validator.handle(String.format("%s %s", transactionType, MONTHS_PER_YEAR)));
    }

    @Test
    protected void the_third_argument_of_time_travel_transactions_are_months() {
        assertFalse(validator.handle(String.format("%s %s", transactionType, "")));
        assertFalse(validator.handle(String.format("%s %s", transactionType, "months")));
        assertTrue(validator.handle(String.format("%s %s", transactionType, getMonthsPerYear())));
    }

    @Test
    protected void months_should_be_between_1_and_60_inclusive() {
        assertFalse(validator.handle(String.format("%s %s", transactionType, -60)));
        assertFalse(validator.handle(String.format("%s %s", transactionType, -3)));
        assertTrue(validator.handle(String.format("%s %s", transactionType, 1)));
        assertTrue(validator.handle(String.format("%s %s", transactionType, 5)));
        assertTrue(validator.handle(String.format("%s %s", transactionType, 30)));

        assertTrue(validator.handle(String.format("%s %s", transactionType, 40)));
        assertTrue(validator.handle(String.format("%s %s", transactionType, 56)));
        assertTrue(validator.handle(String.format("%s %s", transactionType, 60)));
        assertFalse(validator.handle(String.format("%s %s", transactionType, 64)));
        assertFalse(validator.handle(String.format("%s %s", transactionType, 120)));
    }

    @Test
    protected void time_travel_validators_can_ignore_additional_arguments() {
        assertTrue(validator.handle(String.format("%s %s %s", transactionType, MONTHS_PER_YEAR, "0")));
        assertTrue(validator.handle(String.format("%s %s %s %s %s %s", transactionType, MONTHS_PER_YEAR, 89, 23892398, 92839233, 23)));
    }

    @Test
    protected void time_travel_validators_can_case_insensitive() {
        assertTrue(validator.handle(String.format("%s %s", "tImE tRaVel", MONTHS_PER_YEAR)));
    }

    @Test
    protected void time_travel_validators_can_be_in_a_chain_of_responsibility() {
        AccountType accountType = AccountType.SAVINGS;
        String id = "17439742";
        double depositAmount = bank.getAccount(SAVINGS_ID).getMaxDepositAmount();

        validator.setNext(new CreateValidator(bank));

        assertTrue(validator.handle(String.format("%s %s %s %s", TransactionType.Create, accountType, id, apr)));
        assertFalse(validator.handle(String.format("%s %s %s", TransactionType.Deposit, SAVINGS_ID, depositAmount)));
    }

    @Test
    protected void rename_me_0() {
        String id = SAVINGS_ID;
        double withdrawAmount = bank.getAccount(id).getMaxWithdrawAmount();
        String transaction = String.format("%s %s %s", TransactionType.Withdraw, id, withdrawAmount);
        validator.setNext(new WithdrawValidator(bank));

        assertTrue(validator.handle(transaction));

        bank.withdraw(id, withdrawAmount);
        assertFalse(validator.handle(transaction));

        bank.timeTravel(1);
        assertTrue(validator.handle(transaction));
    }

    @Test
    protected void rename_me_1() {
        String payingID = SAVINGS_ID;
        String receivingID = CHECKING_ID;
        double transferAmount = min(bank.getAccount(payingID).getMaxWithdrawAmount(), bank.getAccount(receivingID).getMaxDepositAmount());
        String transaction = String.format("%s %s %s %s", TransactionType.Transfer, payingID, receivingID, transferAmount);
        validator.setNext(new TransferValidator(bank));

        assertTrue(validator.handle(transaction));

        bank.transfer(payingID, receivingID, transferAmount);
        assertFalse(validator.handle(transaction));

        bank.timeTravel(1);
        assertTrue(validator.handle(transaction));
    }

    @Test
    protected void rename_me_2() {
        validator.setNext(new WithdrawValidator(bank));

        for (int month = 0; month < MONTHS_PER_YEAR * 2; month++) {
            assertEquals(month >= MONTHS_PER_YEAR, validator.handle(String.format("%s %s %s", TransactionType.Withdraw, CD_ID, bank.getAccount(CD_ID).getMaxWithdrawAmount())));

            bank.timeTravel(1);
        }
    }

    @Test
    protected void rename_me_3() {
        validator.setNext(new TransferValidator(bank));

        for (int month = 0; month < MONTHS_PER_YEAR * 2; month++) {
            assertEquals(month >= MONTHS_PER_YEAR, validator.handle(String.format("%s %s %s %s", TransactionType.Transfer, CD_ID, SAVINGS_ID, bank.getAccount(SAVINGS_ID).getMaxDepositAmount())));

            bank.timeTravel(1);
        }
    }
}
