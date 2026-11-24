package com.ledger.engine.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.ledger.engine.entity.CurrencyCode;
import com.ledger.engine.entity.TransactionStatus;
import com.ledger.engine.entity.TransactionType;

public record TransactionResponse(

    Long id,
    String referenceCode,
    TransactionType transactionType,
    TransactionStatus status,
    BigDecimal amount,
    CurrencyCode currency,
    LocalDateTime createdAt

) {}