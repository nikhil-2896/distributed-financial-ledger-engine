package com.ledger.engine.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.ledger.engine.entity.CurrencyCode;
import com.ledger.engine.entity.EntryType;
import com.ledger.engine.entity.TransactionStatus;
import com.ledger.engine.entity.TransactionType;

public record LedgerEntryResponse(

    Long entryId,
    String transactionReference,
    TransactionType transactionType,
    TransactionStatus transactionStatus,
    EntryType entryType,
    BigDecimal amount,
    CurrencyCode currency,
    LocalDateTime transactionDate

) {}