package com.sefault.server.finance.dto.record;

import com.sefault.server.finance.entity.Payroll;
import com.sefault.server.finance.enums.TransactionType;
import com.sefault.server.sales.entity.Sale;

import java.time.LocalDateTime;
import java.util.UUID;

public record TransactionRecord(
        UUID id,
        TransactionType type,
        Sale sale,
        Payroll payroll,
        Double amount,
        LocalDateTime createdAt
) {
}

