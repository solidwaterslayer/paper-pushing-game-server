package server.game.pushing.paper.order_factory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.game.pushing.paper.order_factory.transaction_factory.CreateFactory;
import server.game.pushing.paper.order_factory.transaction_factory.DepositFactory;
import server.game.pushing.paper.order_factory.transaction_factory.TransactionFactory;
import server.game.pushing.paper.store.bank.Bank;
import server.game.pushing.paper.store.bank.account.AccountType;
import server.game.pushing.paper.store.chain_of_responsibility.ChainOfResponsibility;
import server.game.pushing.paper.store.chain_of_responsibility.ChainOfResponsibilityFactory;
import server.game.pushing.paper.store.chain_of_responsibility.TransactionType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.*;

public class TransactionFactoryTests {
    private Bank bank;
    private List<TransactionFactory> transactionFactories;
    private ChainOfResponsibility validator;
    private ChainOfResponsibility processor;

    private Logger logger;

    @BeforeEach
    protected void setUp() {
        bank = new Bank();
        Random random = new Random();
        transactionFactories = new ArrayList<>();
        ChainOfResponsibilityFactory chainOfResponsibilityFactory = new ChainOfResponsibilityFactory(bank);
        validator = chainOfResponsibilityFactory.getChainOfResponsibility(true);
        processor = chainOfResponsibilityFactory.getChainOfResponsibility(false);

        logger = LoggerFactory.getLogger(this.getClass());

        transactionFactories.add(new CreateFactory(bank, random));
        transactionFactories.add(new DepositFactory(bank, random));
    }

    @Test
    protected void get_transaction_should_return_a_valid_and_random_transaction() throws Exception {
        for (TransactionFactory transactionFactory : transactionFactories) {
            getTransaction(transactionFactory);
        }
    }

    private void getTransaction(TransactionFactory transactionFactory) throws Exception {
        for (int i = 0; i < 9999; i++) {
            String transaction = transactionFactory.getTransaction();

            logger.info(String.format("[test %s] %s", i, transaction));
            assertTrue(validator.handle(transaction) && processor.handle(transaction));
        }
    }

    @Test
    protected void deposit_factory_when_bank_contains_0_checking_or_savings_should_throw_exception() {
        assertEquals("[error] bank is empty", assertThrows(Exception.class, transactionFactories.get(1)::getTransaction).getMessage());

        for (int i = 0; i < 9; i++) {
            processor.handle(String.format("%s %s %s %s %s", TransactionType.Create, AccountType.CD, "0000000" + i, bank.getMaxAPR(), bank.getMinInitialCDBalance()));
            assertEquals("[error] bank contains only cd", assertThrows(Exception.class, transactionFactories.get(1)::getTransaction).getMessage());
        }
    }
}
