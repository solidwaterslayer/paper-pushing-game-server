package server.game.pushing.paper.order_generator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.game.pushing.paper.order_generator.transaction_generator.*;
import server.game.pushing.paper.store.bank.AccountType;
import server.game.pushing.paper.store.bank.Bank;
import server.game.pushing.paper.store.handler.ChainOfResponsibilityFactory;
import server.game.pushing.paper.store.handler.Handler;
import server.game.pushing.paper.store.handler.TransactionType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class TransactionGeneratorTests {
    private Bank bank;
    private List<TransactionGenerator> transactionFactories;
    private Handler validator;
    private Handler processor;

    private Logger logger;

    @BeforeEach
    protected void setUp() {
        bank = new Bank();
        Random random = new Random(0);
        transactionFactories = new ArrayList<>();
        ChainOfResponsibilityFactory chainOfResponsibilityFactory = new ChainOfResponsibilityFactory(bank);
        validator = chainOfResponsibilityFactory.getValidator();
        processor = chainOfResponsibilityFactory.getProcessor();

        logger = LoggerFactory.getLogger(this.getClass());

        transactionFactories.addAll(Arrays.asList(new CreateGenerator(bank, random), new DepositGenerator(bank, random), new WithdrawGenerator(bank, random), new TransferGenerator(bank, random), new TimeTravelGenerator(bank, random)));
    }

    @Test
    protected void transaction_factories_should_return_a_valid_transaction() {
        for (TransactionGenerator transactionGenerator : transactionFactories) {
            getTransaction(transactionGenerator);
        }
    }

    private void getTransaction(TransactionGenerator transactionGenerator) {
        for (int i = 0; i < 999; i++) {
            String transaction = transactionGenerator.getTransaction();

            logger.info(String.format("[%s] %s", i, transaction));
            assertTrue(validator.handleTransaction(transaction) && processor.handleTransaction(transaction));
        }
    }

    private void create_factories_can_return_a_valid_but_loaded_transaction() {
        for (AccountType accountType : AccountType.values()) {
            for (int i = 0; i < 333; i++) {
                String transaction = ((CreateGenerator) transactionFactories.get(0)).getLoadedTransaction(accountType);

                logger.info(String.format("[%s] %s", i, transaction));
                assertTrue(validator.handleTransaction(transaction) && processor.handleTransaction(transaction));
                assertTrue(transaction.contains(accountType.toString().toLowerCase()));
            }
        }
    }

    @Test
    protected void create_factories_can_not_support_more_than_1000_create_transactions() {
        create_factories_can_return_a_valid_but_loaded_transaction();

        String transaction = transactionFactories.get(0).getTransaction();
        assertTrue(validator.handleTransaction(transaction) && processor.handleTransaction(transaction));
        assertEquals("create factories can not support more than 1000 create transactions", assertThrows(IllegalArgumentException.class, transactionFactories.get(0) :: getTransaction).getMessage());
    }

    @Test
    protected void deposit_withdraw_and_transfer_factories_should_throw_an_illegal_argument_exception_when_the_bank_contains_less_than_2_checking_accounts() {
        TransactionType transactionType = TransactionType.Create;
        double minCDBalance = bank.getMinCDBalance();

        processor.handleTransaction(String.format("%s %s %s", transactionType, AccountType.Checking, "11111111"));
        for (int i = 0; i < 9; i++) {
            for (int j = 1; j < 4; j++) {
                assertEquals("the bank contains less than 2 checking accounts", assertThrows(IllegalArgumentException.class, transactionFactories.get(j) :: getTransaction).getMessage());
            }

            processor.handleTransaction(String.format("%s %s %s", transactionType, AccountType.Savings, "0000000" + i));
            processor.handleTransaction(String.format("%s %s %s %s", transactionType, AccountType.CD, "0000000" + i, minCDBalance));
        }

        processor.handleTransaction(String.format("%s %s %s", transactionType, AccountType.Checking, "11111110"));
        for (int i = 1; i < 4; i++) {
            String transaction = transactionFactories.get(i).getTransaction();
            assertTrue(validator.handleTransaction(transaction) && processor.handleTransaction(transaction));
        }
    }
}
