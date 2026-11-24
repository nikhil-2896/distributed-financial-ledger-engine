
package com.ledger.engine.dto;

import java.math.BigDecimal;

import com.ledger.engine.entity.AccountStatus;
import com.ledger.engine.entity.CurrencyCode;

public record AccountResponse(

    Long id,
    String accountNumber,
    CurrencyCode currency,
    BigDecimal balance,
    AccountStatus status,
    Long customerId

) {}
