package com.sefault.server.finance.dto.projection;

import com.sefault.server.finance.entity.Payroll;
import com.sefault.server.finance.enums.TransactionType;
import com.sefault.server.sales.entity.Sale;

import java.time.LocalDateTime;
import java.util.UUID;

public interface TransactionProjection {
    UUID getId();

    TransactionType getType();

    Sale getSale();

    Payroll getPayroll();

    Double getAmount();

    LocalDateTime getCreatedAt();
}