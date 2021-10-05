package server.game.pushing.paper.game_level;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class GameLevelController {
    @GetMapping
    public @ResponseBody ResponseEntity<GameLevel> getGameLevel() {
        return new ResponseEntity<>(new GameLevel(), HttpStatus.OK);
    }
}
