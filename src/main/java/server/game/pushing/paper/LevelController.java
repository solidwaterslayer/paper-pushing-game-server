package server.game.pushing.paper;

import server.game.pushing.paper.bank.Bank;
import server.game.pushing.paper.bank.account.Checking;
import server.game.pushing.paper.bank.account.Savings;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
