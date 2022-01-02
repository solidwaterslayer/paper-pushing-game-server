package server.game.pushing.paper;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import server.game.pushing.paper.generator.DepositGenerator;
import server.game.pushing.paper.generator.OrderGenerator;
import server.game.pushing.paper.generator.TransferGenerator;
import server.game.pushing.paper.generator.WithdrawGenerator;
import server.game.pushing.paper.store.Store;
import server.game.pushing.paper.store.bank.Bank;

import java.util.Hashtable;
import java.util.List;
import java.util.Random;

import static java.lang.Math.min;
import static org.junit.jupiter.api.Assertions.*;
import static server.game.pushing.paper.TransactionType.*;
import static server.game.pushing.paper.store.bank.AccountType.Checking;

public class GeneratorTests {
    private Logger logger;
    private Random random;

    @BeforeEach
    protected void setUp() {
        logger = LoggerFactory.getLogger(this.getClass());
        random = new Random();
    }

    private void updateCount(String string, Hashtable<String, Integer> count) {
        if (count.containsKey(string)) {
            count.put(string, count.get(string) + 1);
        } else {
            count.put(string, 0);
        }
    }

    private boolean isOrderValid(List<String> order) {
        for (String transaction : (new Store() {{ setOrder(order); }}).getReceipt()) {
            if (transaction.contains("[invalid]")) {
                return false;
            }
        }

        return true;
    }

    @Test
    protected void an_order_generator_generates_random_and_valid_transactions() {
        int size = 200;
        OrderGenerator orderGenerator = new OrderGenerator();
        List<String> order = orderGenerator.generateOrder(random, size);

        String maxKey = null;
        int maxValue = 0;
        Hashtable<String, Integer> transactionCount = new Hashtable<>();
        int minValue = Integer.MAX_VALUE;
        Hashtable<String, Integer> typeCount = new Hashtable<>();
        for (String transaction : order) {
            String type = transaction.split(" ")[0];

            updateCount(transaction, transactionCount);
            updateCount(type, typeCount);
            if (transactionCount.get(transaction) > maxValue) {
                maxKey = transaction;
                maxValue = transactionCount.get(transaction);
            }
        }

        logger.info(String.format("%s %s %sx", "[random 0]", maxKey, maxValue));
        for (String type : typeCount.keySet()) {
            logger.info(String.format("%s %s %sx", "[random 1]", type, typeCount.get(type)));
            minValue = min(minValue, typeCount.get(type));
        }
        assertTrue(maxValue < 9);
        assertTrue(minValue > 0);
        assertTrue(isOrderValid(order));
    }

    @Test
    protected void the_first_2_transactions_are_create_checking_transactions() {
        int size = 2;
        Bank bank = new Bank() {{ createSavingsAccount("98430842"); createSavingsAccount("98439843"); }};
        OrderGenerator orderGenerator = new OrderGenerator();
        for (int i = 0; i < 99; i++) {
            List<String> order = orderGenerator.generateOrder(random, size);

            for (String transaction : order) {
                logger.info(String.format("[%s %s] %s", "2 create checking transactions", i, transaction));
                assertTrue(transaction.contains(Checking.name().toLowerCase()));
            }
        }

        String message = "generators should have 2 checking accounts";
        assertEquals(message, assertThrows(IllegalArgumentException.class, new DepositGenerator(random, bank) :: generateTransaction).getMessage());
        assertEquals(message, assertThrows(IllegalArgumentException.class, new WithdrawGenerator(random, bank) :: generateTransaction).getMessage());
        assertEquals(message, assertThrows(IllegalArgumentException.class, new TransferGenerator(random, bank) :: generateTransaction).getMessage());
    }

    @Test
    protected void deposit_withdraw_and_transfer_transactions_use_amounts_divisible_by_100() {
        int size = 100;
        OrderGenerator orderGenerator = new OrderGenerator();
        List<String> order = orderGenerator.generateOrder(random, size);

        for (int i = 0; i < size; i++) {
            String transaction = order.get(i);
            String[] transactionArguments = transaction.split(" ");
            String transactionType = transactionArguments[0];
            String amount = transactionArguments[transactionArguments.length - 1];

            if (transactionType.equalsIgnoreCase(Deposit.name())
                    || transactionType.equalsIgnoreCase(Withdraw.name())
                    || transactionType.equalsIgnoreCase(Transfer.name())) {
                logger.info(String.format("[%s %s] %s", "divisible by 100", i, transaction));
                assertTrue(amount.endsWith("00.00"));
            }
        }
    }
}
