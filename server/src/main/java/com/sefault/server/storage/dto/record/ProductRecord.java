package com.sefault.server.storage.dto.record;

import java.time.LocalDateTime;
import java.util.UUID;

public record ProductRecord(
        UUID id, String name, UUID productCategoryId, UUID imageId, LocalDateTime createdAt, LocalDateTime updatedAt) {}
