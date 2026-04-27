package com.sefault.server.storage.dto.record;

import java.time.LocalDateTime;
import java.util.UUID;

public record ProductVariationRecord(
        UUID id,
        String sku,
        Double price,
        UUID productId,
        Integer quantity,
        UUID imageId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {}
