package com.sefault.server.sales.dto.record;

import java.time.LocalDateTime;
import java.util.UUID;

public record SaleRecord(
        UUID id,
        Double discount,
        UUID employeeId,
        Boolean refunded,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {}
