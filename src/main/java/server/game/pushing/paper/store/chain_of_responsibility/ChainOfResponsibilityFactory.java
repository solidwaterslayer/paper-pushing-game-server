package server.game.pushing.paper.store.chain_of_responsibility;

import server.game.pushing.paper.store.bank.Bank;
import server.game.pushing.paper.store.chain_of_responsibility.transaction_processor.*;
import server.game.pushing.paper.store.chain_of_responsibility.transaction_validator.*;

import java.util.Arrays;
import java.util.List;

public class ChainOfResponsibilityFactory {
    private final Bank BANK;

    public ChainOfResponsibilityFactory(Bank bank) {
        this.BANK = bank;
    }

    public ChainOfResponsibility getChainOfResponsibility(boolean isValidator) {
        List<ChainOfResponsibility> chainOfResponsibility;

        if (isValidator) {
            chainOfResponsibility = Arrays.asList(
                    new CreateValidator(BANK),
                    new DepositValidator(BANK),
                    new WithdrawValidator(BANK),
                    new TransferValidator(BANK),
                    new PassTimeValidator(BANK)
            );
        } else {
            chainOfResponsibility = Arrays.asList(
                    new CreateProcessor(BANK),
                    new DepositProcessor(BANK),
                    new WithdrawProcessor(BANK),
                    new TransferProcessor(BANK),
                    new PassTimeProcessor(BANK)
            );
        }

        for (int i = 0; i < chainOfResponsibility.size() - 1; i++) {
            chainOfResponsibility.get(i).setNext(chainOfResponsibility.get(i + 1));
        }

        return chainOfResponsibility.get(0);
    }
}
