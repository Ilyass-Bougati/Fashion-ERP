package com.sefault.server.finance.dto.record;

import com.sefault.server.finance.enums.TransactionType;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransactionRecord(UUID id, TransactionType type, UUID saleId, Double amount, LocalDateTime createdAt) {}
