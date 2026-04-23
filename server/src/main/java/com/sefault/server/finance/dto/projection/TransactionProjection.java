package com.sefault.server.finance.dto.projection;

import com.sefault.server.finance.enums.TransactionType;
import java.time.LocalDateTime;
import java.util.UUID;

public interface TransactionProjection {
    UUID getId();

    TransactionType getType();

    UUID getSaleId();

    Double getAmount();

    LocalDateTime getCreatedAt();
}
