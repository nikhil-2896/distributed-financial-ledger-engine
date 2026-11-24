package com.ledger.engine.controller;

import com.ledger.engine.dto.AccountResponse;
import com.ledger.engine.service.AccountService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/accounts")
public class AccountStatusController {

    private final AccountService accountService;

    public AccountStatusController(AccountService accountService) {
        this.accountService = accountService;
    }

    @PatchMapping("/{accountId}/freeze")
    public ResponseEntity<AccountResponse> freezeAccount(
            @PathVariable Long accountId) {
        return ResponseEntity.ok(accountService.freezeAccount(accountId));
    }

    @PatchMapping("/{accountId}/unfreeze")
    public ResponseEntity<AccountResponse> unfreezeAccount(
            @PathVariable Long accountId) {
        return ResponseEntity.ok(accountService.unfreezeAccount(accountId));
    }
}