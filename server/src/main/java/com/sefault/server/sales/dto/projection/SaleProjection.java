package com.sefault.server.sales.dto.projection;

import com.sefault.server.sales.SaleStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public interface SaleProjection {
    UUID getId();

    Double getDiscount();

    UUID getEmployeeId();

    SaleStatus getStatus();

    LocalDateTime getCreatedAt();

    LocalDateTime getUpdatedAt();
}
