package server.game.pushing.paper.ledgervalidator;

import server.game.pushing.paper.ledgervalidator.bank.Bank;
import server.game.pushing.paper.ledgervalidator.chainofresponsibility.ChainOfResponsibility;
import server.game.pushing.paper.ledgervalidator.chainofresponsibility.transactionvalidator.*;

import java.util.Arrays;
import java.util.List;

public class LedgerValidator {
    protected ChainOfResponsibility validator;
    protected ChainOfResponsibility processor;
    protected Bank bank;

    public LedgerValidator(Bank bank) {
        validator = ChainOfResponsibility.getInstance(Arrays.asList(
                new CreateValidator(bank),
                new DepositValidator(bank),
                new WithdrawValidator(bank),
                new TransferValidator(bank),
                new PassTimeValidator(bank),
                null
        ));
        processor = ChainOfResponsibility.getInstance(Arrays.asList(
                new CreateValidator(bank),
                new DepositValidator(bank),
                new WithdrawValidator(bank),
                new TransferValidator(bank),
                new PassTimeValidator(bank),
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
