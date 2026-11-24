package com.ledger.engine.dto;

import java.math.BigDecimal;

import com.ledger.engine.entity.TransactionType;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

public record TransactionRequest(

    @NotNull(message = "Transaction type is required")
    TransactionType transactionType,

    @NotNull(message = "Account ID is required")
    Long accountId,

    Long destinationAccountId,

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.0001", message = "Amount must be greater than zero")
    @Digits(integer = 15, fraction = 4,
            message = "Amount can have up to 15 integer digits and 4 decimal places")
    BigDecimal amount

) {}