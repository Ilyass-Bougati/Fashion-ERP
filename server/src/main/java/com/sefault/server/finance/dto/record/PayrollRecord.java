package com.sefault.server.finance.dto.record;

import java.time.LocalDateTime;
import java.util.UUID;

public record PayrollRecord(
        UUID id, Double salary, UUID transactionId, UUID employeeId, Double commission, LocalDateTime createdAt) {}
