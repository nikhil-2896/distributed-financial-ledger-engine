package com.ledger.engine.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ledger.engine.dto.TransactionRequest;
import com.ledger.engine.dto.TransactionResponse;
import com.ledger.engine.service.TransactionService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(
            TransactionService transactionService) {

        this.transactionService = transactionService;
    }

    // Create a deposit, withdrawal or transfer
    @PostMapping
    public ResponseEntity<TransactionResponse> processTransaction(
            @Valid @RequestBody TransactionRequest request,
            @RequestHeader("Idempotency-Key") String idempotencyKey) {

        TransactionResponse response =
                transactionService.processTransaction(
                        request, idempotencyKey);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    // Get transaction using reference code
    @GetMapping("/{referenceCode}")
    public ResponseEntity<TransactionResponse> getTransactionByReference(
            @PathVariable String referenceCode) {

        TransactionResponse response =
                transactionService.getTransactionByReference(referenceCode);

        return ResponseEntity.ok(response);
    }
}