package server.game.pushing.paper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import server.game.pushing.paper.bank.Bank;
import server.game.pushing.paper.processor.transaction.*;
import server.game.pushing.paper.validator.transaction.*;

import java.util.Arrays;
import java.util.List;

import static server.game.pushing.paper.TransactionChain.createTransactionChain;

public class Exchanger {
    protected TransactionChain validator;
    protected TransactionChain processor;
    protected Bank bank;

    public Exchanger(Bank bank) {
        validator = createTransactionChain(Arrays.asList(
                new CreateValidator(bank),
                new DepositValidator(bank),
                new WithdrawValidator(bank),
                new TransferValidator(bank),
                new PassTimeValidator(bank),
                null
        ));
        processor = createTransactionChain(Arrays.asList(
                new CreateProcessor(bank),
                new DepositProcessor(bank),
                new WithdrawProcessor(bank),
                new TransferProcessor(bank),
                new PassTimeProcessor(bank),
                null
        ));
        this.bank = bank;
    }

    public List<String> exchange(List<String> ledger) {
        TransactionHistory transactionHistory = new TransactionHistory(bank);

        for (String transaction : ledger) {
            transactionHistory.addTransaction(transaction, validator.handle(transaction) && processor.handle(transaction));
        }

        return transactionHistory.getTransactions();
    }
}
