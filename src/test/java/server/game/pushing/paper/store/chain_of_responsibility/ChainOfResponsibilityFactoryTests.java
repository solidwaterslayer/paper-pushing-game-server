package server.game.pushing.paper.store.chain_of_responsibility;

import org.junit.jupiter.api.Test;
import server.game.pushing.paper.store.bank.Bank;
import server.game.pushing.paper.store.bank.account.AccountType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;

import static java.lang.Math.min;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static server.game.pushing.paper.store.bank.Bank.getMonthsPerYear;

public class ChainOfResponsibilityFactoryTests {
    @Test
    protected void temp() {
        Bank bank = new Bank();
        ChainOfResponsibilityFactory chainOfResponsibilityFactory = new ChainOfResponsibilityFactory(bank);
        ChainOfResponsibility validator = chainOfResponsibilityFactory.getChainOfResponsibility(true);
        ChainOfResponsibility processor = chainOfResponsibilityFactory.getChainOfResponsibility(false);

        int months = getMonthsPerYear();
        String fromID = "98340842";
        String toID = "08429843";
        double apr = bank.getMaxAPR();
        String transaction0 = String.format("%s %s %s %s", TransactionType.Create, AccountType.Checking, fromID, apr);
        assertTrue(validator.handle(transaction0));
        assertTrue(processor.handle(transaction0));
        assertTrue(processor.handle(String.format("%s %s %s %s", TransactionType.Create, AccountType.Savings, toID, apr)));
        double depositAmount = bank.getAccount(fromID).getMaxDepositAmount();
        double withdrawAmount = bank.getAccount(fromID).getMaxWithdrawAmount();
        double transferAmount = min(bank.getAccount(fromID).getMaxWithdrawAmount(), bank.getAccount(toID).getMaxDepositAmount());

        List<ChainOfResponsibility> chainOfResponsibility0 = new ArrayList<>(Arrays.asList(validator, processor));
        List<String> order = new ArrayList<>(Arrays.asList(
                String.format("%s %s %s", TransactionType.Deposit, fromID, depositAmount),
                String.format("%s %s %s", TransactionType.Withdraw, fromID, withdrawAmount),
                String.format("%s %s %s %s", TransactionType.Transfer, fromID, toID, transferAmount),
                String.format("%s %s", TransactionType.PassTime, months)
        ));
        for (String transaction1 : order) {
            for (ChainOfResponsibility chainOfResponsibility1 : chainOfResponsibility0) {
                assertTrue(chainOfResponsibility1.handle(transaction1));
            }
        }
    }
}
