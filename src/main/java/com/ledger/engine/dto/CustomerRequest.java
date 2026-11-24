
package com.ledger.engine.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CustomerRequest(

    @NotBlank(message = "Name is required")
    String fullName,

    @NotBlank(message = "Email is required")
    @Email(message = "Enter a valid email")
    String email

) {}
