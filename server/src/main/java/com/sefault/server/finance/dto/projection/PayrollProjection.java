package com.sefault.server.finance.dto.projection;

import java.time.LocalDateTime;
import java.util.UUID;

public interface PayrollProjection {
    UUID getId();

    Double getSalary();

    UUID getTransactionId();

    UUID getEmployeeId();

    Double getCommission();

    LocalDateTime getCreatedAt();
}
