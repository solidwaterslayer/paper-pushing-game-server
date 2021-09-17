package server.game.pushing.paper;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import server.game.pushing.paper.ledgervalidator.bank.Bank;
import server.game.pushing.paper.ledgervalidator.bank.account.Checking;
import server.game.pushing.paper.ledgervalidator.bank.account.Savings;

import java.util.ArrayList;
import java.util.Arrays;

@RestController
@RequestMapping("/")
public class LevelController {
    @GetMapping
    public @ResponseBody ResponseEntity<Bank> getLevel() {
        return new ResponseEntity<>(new Bank(new ArrayList<>(Arrays.asList(
                new Checking("34782479", 0.6),
                new Savings("72497834", 0.7)
        ))), HttpStatus.OK);
    }
}
