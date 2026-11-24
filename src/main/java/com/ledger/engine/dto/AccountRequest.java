
package com.ledger.engine.dto;

import com.ledger.engine.entity.CurrencyCode;

import jakarta.validation.constraints.NotNull;

public record AccountRequest(

    @NotNull(message = "Customer ID is required")
    Long customerId,

    @NotNull(message = "Currency is required")
    CurrencyCode currency

) {}
