package com.sefault.server.sales.dto.record;

import com.sefault.server.finance.entity.Transaction;
import com.sefault.server.hr.entity.Employee;
import com.sefault.server.sales.entity.SaleLine;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record SaleRecord(
        UUID id,
        Double discount,
        List<Transaction> transactions,
        List<SaleLine> saleLines,
        Employee employee,
        Boolean refunded,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}

