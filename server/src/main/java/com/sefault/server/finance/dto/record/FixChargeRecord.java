package com.sefault.server.finance.dto.record;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.time.LocalDateTime;
import java.util.UUID;

public record FixChargeRecord(
        UUID id,

        @NotBlank(message = "Charge name cannot be blank")
        String name,

        String description,

        @NotNull(message = "Amount is required")
        @PositiveOrZero(message = "Amount cannot be negative")
        Double amount,

        Boolean active,

        LocalDateTime createdAt) {
    public FixChargeRecord {
        if (active == null) {
            active = true;
        }
    }
}
