package server.game.pushing.paper.store.chain_of_responsibility;

import org.junit.jupiter.api.Test;
import server.game.pushing.paper.store.bank.Bank;
import server.game.pushing.paper.store.bank.AccountType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.lang.Math.min;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static server.game.pushing.paper.store.bank.Bank.getMonthsPerYear;

public class ChainOfResponsibilityFactoryTests {
    @Test
    protected void chain_of_responsibility_factories_should_return_every_validator_or_every_processor() {
        Bank bank = new Bank();
        ChainOfResponsibilityFactory chainOfResponsibilityFactory = new ChainOfResponsibilityFactory(bank);
        ChainOfResponsibility validator = chainOfResponsibilityFactory.getChainOfResponsibility(true);
        ChainOfResponsibility processor = chainOfResponsibilityFactory.getChainOfResponsibility(false);

        int months = getMonthsPerYear();
        String payingID = "98340842";
        String receivingID = "08429843";
        String transaction0 = String.format("%s %s %s", TransactionType.Create, AccountType.Checking, payingID);
        assertTrue(validator.handle(transaction0));
        assertTrue(processor.handle(transaction0));
        assertTrue(processor.handle(String.format("%s %s %s", TransactionType.Create, AccountType.Savings, receivingID)));
        double depositAmount = bank.getAccount(payingID).getMaxDepositAmount();
        double withdrawAmount = bank.getAccount(payingID).getMaxWithdrawAmount();
        double transferAmount = min(bank.getAccount(payingID).getMaxWithdrawAmount(), bank.getAccount(receivingID).getMaxDepositAmount());

        List<ChainOfResponsibility> chainOfResponsibility0 = new ArrayList<>(Arrays.asList(validator, processor));
        List<String> order = new ArrayList<>(Arrays.asList(
                String.format("%s %s %s", TransactionType.Deposit, payingID, depositAmount),
                String.format("%s %s %s", TransactionType.Withdraw, payingID, withdrawAmount),
                String.format("%s %s %s %s", TransactionType.Transfer, payingID, receivingID, transferAmount),
                String.format("%s %s", TransactionType.TimeTravel, months)
        ));
        for (String transaction1 : order) {
            for (ChainOfResponsibility chainOfResponsibility1 : chainOfResponsibility0) {
                assertTrue(chainOfResponsibility1.handle(transaction1));
            }
        }
    }
}
