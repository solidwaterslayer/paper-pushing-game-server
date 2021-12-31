package server.game.pushing.paper.store.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import server.game.pushing.paper.store.bank.Bank;
import server.game.pushing.paper.store.handler.processor.TransferProcessor;
import server.game.pushing.paper.store.handler.validator.TransferValidator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static server.game.pushing.paper.store.bank.AccountType.Savings;
import static server.game.pushing.paper.store.handler.TransactionType.Create;

public class HandlerTests {
    private Bank bank;
    private Handler validator;
    private Handler processor;

    @BeforeEach
    protected void setUp() {
        bank = new Bank();
        ChainOfResponsibility chainOfResponsibility = new ChainOfResponsibility(bank);
        validator = chainOfResponsibility.getValidator();
        processor = chainOfResponsibility.getProcessor();
    }

    @Test
    protected void transaction_processors_and_validators_are_handlers_in_a_chain_of_responsibility() {
        Handler transferValidator = new TransferValidator(bank);
        Handler transferProcessor = new TransferProcessor(bank);
        String validTransaction = String.format("%s %s %s", Create, Savings, "87549753");
        String invalidTransaction = "totally a valid transaction";

        transferValidator.setNext(validator);
        transferProcessor.setNext(processor);

        assertTrue(transferValidator.handleTransaction(validTransaction));
        assertTrue(transferProcessor.handleTransaction(validTransaction));
        assertFalse(transferValidator.handleTransaction(invalidTransaction));
        assertFalse(transferProcessor.handleTransaction(invalidTransaction));
    }

    @Test
    protected void handlers_are_case_insensitive_and_can_ignore_extra_arguments() {
        String payingID = "98340842";
        String receivingID = "08429843";
        List<String> order = new ArrayList<>(Arrays.asList(
                String.format("%s %s %s %s", "create", "SAVINGS", payingID, false),
                String.format("%s %s %s %s %s", "CREATE", "CHECKING", receivingID, "bob", validator),
                String.format("%s %s %s %s %s %s %s", "transfer", payingID, receivingID, 900, true, processor, "negative bob"),
                String.format("%s %s %s %s %s %s", "TIME travel", 2, "the power of friendship", 0, 0, Double.POSITIVE_INFINITY),
                String.format("%s %s %s %s %s %s %s %s", "dEpOsIt", payingID, 300, 283, -31255132, 3, 234, 1235),
                String.format("%s %s %s %s %s %s %s %s %s", "WiThDrAw", receivingID, 300, new ArrayList<>(), bank, new HandlerTests(), "tree", '3', null)
        ));

        for (String transaction : order) {
            assertTrue(validator.handleTransaction(transaction));
            assertTrue(processor.handleTransaction(transaction));
        }
    }
}
