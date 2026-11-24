package com.ledger.engine.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ledger.engine.dto.LedgerEntryResponse;
import com.ledger.engine.service.LedgerEntryService;

@RestController
@RequestMapping("/api/ledger-entries")
public class LedgerEntryController {

    private final LedgerEntryService ledgerEntryService;

    public LedgerEntryController(
            LedgerEntryService ledgerEntryService) {

        this.ledgerEntryService = ledgerEntryService;
    }

    @GetMapping("/account/{accountId}")
    public ResponseEntity<List<LedgerEntryResponse>>
            getAccountHistory(@PathVariable Long accountId) {

        List<LedgerEntryResponse> response =
                ledgerEntryService.getAccountHistory(accountId);

        return ResponseEntity.ok(response);
    }
}