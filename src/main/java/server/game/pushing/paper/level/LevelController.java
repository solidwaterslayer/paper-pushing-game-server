package server.game.pushing.paper.level;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/")
public class LevelController {
    @CrossOrigin(origins = { "https://solidwaterslayer.github.io", "http://localhost:4200" })
    @GetMapping
    public @ResponseBody ResponseEntity<Level> getLevel() {
        return new ResponseEntity<>(new Level(), HttpStatus.OK);
    }
}
