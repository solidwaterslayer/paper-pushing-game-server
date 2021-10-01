package server.game.pushing.paper.order_factory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.game.pushing.paper.order_factory.transaction_factory.*;
import server.game.pushing.paper.store.bank.Bank;
import server.game.pushing.paper.store.bank.account.AccountType;
import server.game.pushing.paper.store.chain_of_responsibility.ChainOfResponsibility;
import server.game.pushing.paper.store.chain_of_responsibility.ChainOfResponsibilityFactory;
import server.game.pushing.paper.store.chain_of_responsibility.TransactionType;

import java.util.ArrayList;
import java.util.Arrays;
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
        Random random = new Random(0);
        transactionFactories = new ArrayList<>();
        ChainOfResponsibilityFactory chainOfResponsibilityFactory = new ChainOfResponsibilityFactory(bank);
        validator = chainOfResponsibilityFactory.getChainOfResponsibility(true);
        processor = chainOfResponsibilityFactory.getChainOfResponsibility(false);

        logger = LoggerFactory.getLogger(this.getClass());

        transactionFactories.addAll(Arrays.asList(new CreateFactory(bank, random), new DepositFactory(bank, random), new WithdrawFactory(bank, random), new TransferFactory(bank, random), new PassTimeFactory(bank, random)));
    }

    @Test
    protected void get_transaction_should_return_a_valid_and_random_transaction() {
        for (TransactionFactory transactionFactory : transactionFactories) {
            getTransaction(transactionFactory);
        }
    }

    private void getTransaction(TransactionFactory transactionFactory) {
        for (int i = 0; i < 999; i++) {
            String transaction = transactionFactory.getTransaction();

            logger.info(String.format("[transaction factory test %s] %s", i, transaction));
            assertTrue(validator.handle(transaction) && processor.handle(transaction));
        }
    }

    @Test
    protected void transfer_factories_when_bank_contains_less_than_2_checking_accounts_should_throw_an_illegal_argument_exception() {
        TransactionType transactionType = TransactionType.Create;
        double apr = bank.getMaxAPR();
        double minInitialCDBalance = bank.getMinInitialCDBalance();

        processor.handle(String.format("%s %s %s %s", transactionType, AccountType.CHECKING, "11111111", apr));
        for (int i = 0; i < 9; i++) {
            for (int j = 1; j < 4; j++) {
                assertEquals("[error] bank contains less than 2 checking accounts", assertThrows(IllegalArgumentException.class, transactionFactories.get(j) :: getTransaction).getMessage());
            }

            processor.handle(String.format("%s %s %s %s", transactionType, AccountType.SAVINGS, "0000000" + i, apr));
            processor.handle(String.format("%s %s %s %s %s", transactionType, AccountType.CD, "0000000" + i, apr, minInitialCDBalance));
        }

        processor.handle(String.format("%s %s %s %s", transactionType, AccountType.CHECKING, "11111110", apr));
        for (int i = 1; i < 4; i++) {
            String transaction = transactionFactories.get(i).getTransaction();
            assertTrue(validator.handle(transaction) && processor.handle(transaction));
        }
    }
}
