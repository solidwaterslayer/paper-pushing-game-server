package server.game.pushing.paper.game_level;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import server.game.pushing.paper.order_factory.OrderFactory;

import java.util.List;
import java.util.Random;

@RestController
@RequestMapping("/")
public class GameLevelController {
    @GetMapping
    public @ResponseBody ResponseEntity<List<String>> getGameLevel() {
        return new ResponseEntity<>((new OrderFactory()).getOrder(11, new Random()), HttpStatus.OK);
    }
}
