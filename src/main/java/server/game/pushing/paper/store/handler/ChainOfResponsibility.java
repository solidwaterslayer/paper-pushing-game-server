package server.game.pushing.paper.store.handler;

import server.game.pushing.paper.store.bank.Bank;
import server.game.pushing.paper.store.handler.processor.*;
import server.game.pushing.paper.store.handler.validator.*;

import java.util.Arrays;
import java.util.List;

public class ChainOfResponsibility {
    private final Bank bank;

    public ChainOfResponsibility(Bank bank) {
        this.bank = bank;
    }

    private void connect(List<Handler> handler) {
        for (int i = 0; i < handler.size() - 1; i++) {
            handler.get(i).setNext(handler.get(i + 1));
        }
    }

    public Handler getValidators() {
        List<Handler> validators = Arrays.asList(
                new CreateValidator(bank),
                new TimeTravelValidator(bank),
                new DepositValidator(bank),
                new WithdrawValidator(bank),
                new TransferValidator(bank)
        );
        connect(validators);
        return validators.get(0);
    }

    public Handler getProcessors() {
        List<Handler> processors = Arrays.asList(
                new CreateProcessor(bank),
                new TimeTravelProcessor(bank),
                new DepositProcessor(bank),
                new WithdrawProcessor(bank),
                new TransferProcessor(bank)
        );
        connect(processors);
        return processors.get(0);
    }
}
