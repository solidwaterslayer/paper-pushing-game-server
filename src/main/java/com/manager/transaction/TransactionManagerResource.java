package com.manager.transaction;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/")
public class TransactionManagerResource {
    @GetMapping
    public @ResponseBody ResponseEntity<List<String>> getTemp() {
        return new ResponseEntity<>(new ArrayList<>(List.of("the power of friendship", "hello world")), HttpStatus.OK);
    }
}
