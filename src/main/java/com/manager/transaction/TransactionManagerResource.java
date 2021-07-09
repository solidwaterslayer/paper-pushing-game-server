package com.manager.transaction;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.manager.transaction.bank.Account;
import com.manager.transaction.bank.Checking;

@RestController
@RequestMapping("/")
public class TransactionManagerResource {
    @GetMapping
    public @ResponseBody ResponseEntity<Account> getTemp() {
        return new ResponseEntity<>(new Checking("78537358", 0.6), HttpStatus.OK);
    }
}
