package server.game.pushing.paper.game_level;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import server.game.pushing.paper.store.bank.account.Account;
import server.game.pushing.paper.store.bank.account.Checking;

@RestController
@RequestMapping("/")
public class GameLevelController {
    @GetMapping
    public @ResponseBody ResponseEntity<Account> getGameLevel() {
        return new ResponseEntity<>(new Checking("34782479", 0.6), HttpStatus.OK);
    }
}
