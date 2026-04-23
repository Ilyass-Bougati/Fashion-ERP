package com.sefault.server.image.dto.record;

import java.time.LocalDateTime;
import java.util.UUID;

public record ImageRecord(
        UUID id,
        String objectKey,
        String bucketName,
        String contentType,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {}
