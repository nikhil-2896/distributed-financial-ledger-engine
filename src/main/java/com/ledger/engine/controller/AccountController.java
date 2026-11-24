
package com.ledger.engine.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ledger.engine.dto.AccountRequest;
import com.ledger.engine.dto.AccountResponse;
import com.ledger.engine.service.AccountService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService accountService;

    public AccountController(AccountService accountService) {
        this.accountService = accountService;
    }

    // Create a new account
    @PostMapping
    public ResponseEntity<AccountResponse> createAccount(
            @Valid @RequestBody AccountRequest request) {

        AccountResponse response =
                accountService.createAccount(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // Get account by ID
    @GetMapping("/{id}")
    public ResponseEntity<AccountResponse> getAccountById(
            @PathVariable Long id) {

        AccountResponse response =
                accountService.getAccountById(id);

        return ResponseEntity.ok(response);
    }

    // Get all accounts of a customer
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<AccountResponse>>
            getAccountsByCustomerId(
                    @PathVariable Long customerId) {

        List<AccountResponse> response =
                accountService.getAccountsByCustomerId(customerId);

        return ResponseEntity.ok(response);
    }
}
