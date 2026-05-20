package com.sefault.server.sales.dto.record;

import com.sefault.server.sales.SaleStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record SaleRecord(
        UUID id,
        Double discount,
        UUID employeeId,
        SaleStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {}
