package com.sefault.server.finance.dto.record;

import java.time.LocalDateTime;
import java.util.UUID;

public record FixChargeRecord(
        UUID id,
        String name,
        String description,
        Double amount,
        Boolean active,
        LocalDateTime createdAt
) {
}

