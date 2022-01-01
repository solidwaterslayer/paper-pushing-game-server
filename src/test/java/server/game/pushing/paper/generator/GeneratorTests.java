package server.game.pushing.paper.generator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.game.pushing.paper.TransactionType;
import server.game.pushing.paper.store.bank.AccountType;
import server.game.pushing.paper.store.bank.Bank;
import server.game.pushing.paper.store.handler.ChainOfResponsibility;
import server.game.pushing.paper.store.handler.Handler;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class GeneratorTests {
    private Bank bank;
    private List<Generator> transactionFactories;
    private Handler validators;
    private Handler processors;

    private Logger logger;

    @BeforeEach
    protected void setUp() {
        bank = new Bank();
        Random random = new Random(0);
        transactionFactories = new ArrayList<>();
        ChainOfResponsibility chainOfResponsibility = new ChainOfResponsibility(bank);
        validators = chainOfResponsibility.getValidators();
        processors = chainOfResponsibility.getProcessors();

        logger = LoggerFactory.getLogger(this.getClass());

        transactionFactories.addAll(Arrays.asList(new CreateGenerator(random, bank), new DepositGenerator(random, bank), new WithdrawGenerator(random, bank), new TransferGenerator(random, bank), new TimeTravelGenerator(random, bank)));
    }

    @Test
    protected void transaction_factories_should_return_a_valid_transaction() {
        for (Generator generator : transactionFactories) {
            getTransaction(generator);
        }
    }

    private void getTransaction(Generator generator) {
        for (int i = 0; i < 999; i++) {
            String transaction = generator.generateTransaction();

            logger.info(String.format("[%s] %s", i, transaction));
            assertTrue(validators.handleTransaction(transaction) && processors.handleTransaction(transaction));
        }
    }

    private void create_factories_can_return_a_valid_but_loaded_transaction() {
        for (AccountType accountType : AccountType.values()) {
            for (int i = 0; i < 333; i++) {
                String transaction = ((CreateGenerator) transactionFactories.get(0)).generateTransaction(accountType);

                logger.info(String.format("[%s] %s", i, transaction));
                assertTrue(validators.handleTransaction(transaction) && processors.handleTransaction(transaction));
                assertTrue(transaction.contains(accountType.toString().toLowerCase()));
            }
        }
    }

    @Test
    protected void create_factories_can_not_support_more_than_1000_create_transactions() {
        create_factories_can_return_a_valid_but_loaded_transaction();

        String transaction = transactionFactories.get(0).generateTransaction();
        assertTrue(validators.handleTransaction(transaction) && processors.handleTransaction(transaction));
        assertEquals("create factories can not support more than 1000 create transactions", assertThrows(IllegalArgumentException.class, transactionFactories.get(0) ::generateTransaction).getMessage());
    }

    @Test
    protected void deposit_withdraw_and_transfer_factories_should_throw_an_illegal_argument_exception_when_the_bank_contains_less_than_2_checking_accounts() {
        TransactionType transactionType = TransactionType.Create;
        double minCDBalance = bank.getMinCDBalance();

        processors.handleTransaction(String.format("%s %s %s", transactionType, AccountType.Checking, "11111111"));
        for (int i = 0; i < 9; i++) {
            for (int j = 1; j < 4; j++) {
                assertEquals("the bank contains less than 2 checking accounts", assertThrows(IllegalArgumentException.class, transactionFactories.get(j) ::generateTransaction).getMessage());
            }

            processors.handleTransaction(String.format("%s %s %s", transactionType, AccountType.Savings, "0000000" + i));
            processors.handleTransaction(String.format("%s %s %s %s", transactionType, AccountType.CD, "0000000" + i, minCDBalance));
        }

        processors.handleTransaction(String.format("%s %s %s", transactionType, AccountType.Checking, "11111110"));
        for (int i = 1; i < 4; i++) {
            String transaction = transactionFactories.get(i).generateTransaction();
            assertTrue(validators.handleTransaction(transaction) && processors.handleTransaction(transaction));
        }
    }
}
