package server.game.pushing.paper.ledgervalidator.transactionchain.transactionprocessor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.game.pushing.paper.ledgervalidator.bank.Bank;
import server.game.pushing.paper.ledgervalidator.bank.account.AccountType;
import server.game.pushing.paper.ledgervalidator.bank.account.Savings;
import server.game.pushing.paper.ledgervalidator.transactionchain.TransactionType;

import static org.junit.jupiter.api.Assertions.*;
import static server.game.pushing.paper.ledgervalidator.bank.Bank.getMaxAPR;
import static server.game.pushing.paper.ledgervalidator.bank.Bank.getMinInitialCDBalance;

public class CreateProcessorTests {
    protected Bank bank;
    CreateProcessor createProcessor;

    @BeforeEach
    protected void setUp() {
        bank = new Bank();
        createProcessor = new CreateProcessor(bank);
    }

    @Test
    protected void create_processor_when_transaction_can_not_process_should_pass_transaction_up_the_chain_of_responsibility() {
        String id = "98430842";
        double apr = getMaxAPR();
        bank.createSavings(id, apr);
        double depositAmount = bank.getAccount(id).getMaxDepositAmount();
        double withdrawAmount = bank.getAccount(id).getMaxWithdrawAmount();

        createProcessor.setNext(new DepositProcessor(bank));

        assertTrue(createProcessor.handle(String.format("%s %s %s", TransactionType.Deposit, id, depositAmount)));
        assertEquals(depositAmount, bank.getAccount(id).getBalance());
        assertFalse(createProcessor.handle(String.format("%s %s %s", TransactionType.Withdraw, id, withdrawAmount)));
    }

    @Test
    protected void create_checking_transaction_should_process() {
        TransactionType transactionType = TransactionType.Create;
        AccountType accountType = AccountType.Checking;
        String id = "87438743";
        double apr = getMaxAPR();

        assertTrue(createProcessor.handle(String.format("%s %s %s %s", transactionType, accountType, id, apr)));
        assertEquals(accountType, bank.getAccount(id).getAccountType());
        assertEquals(id, bank.getAccount(id).getID());
        assertEquals(apr, bank.getAccount(id).getAPR());
        assertEquals(0, bank.getAccount(id).getBalance());
    }

    @Test
    protected void create_savings_transaction_should_process() {
        TransactionType transactionType = TransactionType.Create;
        AccountType accountType = AccountType.Savings;
        String id = "87438742";
        double apr = getMaxAPR();

        assertTrue(createProcessor.handle(String.format("%s %s %s %s", transactionType, accountType, id, apr)));
        assertEquals(accountType, bank.getAccount(id).getAccountType());
        assertEquals(id, bank.getAccount(id).getID());
        assertEquals(apr, bank.getAccount(id).getAPR());
        assertEquals(0, bank.getAccount(id).getBalance());
    }

    @Test
    protected void create_cd_transaction_should_process() {
        TransactionType transactionType = TransactionType.Create;
        AccountType accountType = AccountType.CD;
        String id = "87438778";
        double apr = getMaxAPR();
        double initialCDBalance = getMinInitialCDBalance();

        assertTrue(createProcessor.handle(String.format("%s %s %s %s %s", transactionType, accountType, id, apr, initialCDBalance)));
        assertEquals(accountType, bank.getAccount(id).getAccountType());
        assertEquals(id, bank.getAccount(id).getID());
        assertEquals(apr, bank.getAccount(id).getAPR());
        assertEquals(initialCDBalance, bank.getAccount(id).getBalance());
    }
}