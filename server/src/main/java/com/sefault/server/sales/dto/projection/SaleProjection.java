package com.sefault.server.sales.dto.projection;

import java.time.LocalDateTime;
import java.util.UUID;

public interface SaleProjection {
    UUID getId();

    Double getDiscount();

    UUID getEmployeeId();

    Boolean getRefunded();

    LocalDateTime getCreatedAt();

    LocalDateTime getUpdatedAt();
}
