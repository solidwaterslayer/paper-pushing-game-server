package com.manager.transaction;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.manager.transaction.bank.*;

import java.util.ArrayList;
import java.util.Arrays;

@RestController
@RequestMapping("/")
public class TransactionResource {
    @GetMapping
    public @ResponseBody ResponseEntity<Bank> getTransaction() {
        return new ResponseEntity<>(new Bank(new ArrayList<>(Arrays.asList(
                new Checking("34782479", 0.6),
                new Savings("72497834", 0.7)
        ))), HttpStatus.OK);
    }
}
