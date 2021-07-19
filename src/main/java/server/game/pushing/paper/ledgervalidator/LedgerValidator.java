package server.game.pushing.paper.ledgervalidator;

import server.game.pushing.paper.ledgervalidator.bank.Bank;
import server.game.pushing.paper.ledgervalidator.transactionchain.TransactionChain;
import server.game.pushing.paper.ledgervalidator.transactionchain.transactionprocessor.*;
import server.game.pushing.paper.ledgervalidator.transactionchain.transactionvalidator.*;

import java.util.Arrays;
import java.util.List;

import static server.game.pushing.paper.ledgervalidator.transactionchain.TransactionChain.createTransactionChain;

public class LedgerValidator {
    protected TransactionChain validator;
    protected TransactionChain processor;
    protected Bank bank;

    public LedgerValidator(Bank bank) {
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

    public List<String> validate(List<String> invalidLedger) {
        ValidLedger validLedger = new ValidLedger(bank);

        for (String transaction : invalidLedger) {
            validLedger.addTransaction(transaction, validator.handle(transaction) && processor.handle(transaction));
        }

        return validLedger.getTransactions();
    }
}
