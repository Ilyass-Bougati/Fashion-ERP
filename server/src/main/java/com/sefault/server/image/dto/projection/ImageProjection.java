package com.sefault.server.image.dto.projection;

import java.time.LocalDateTime;
import java.util.UUID;

public interface ImageProjection {
    UUID getId();

    String getObjectKey();

    String getBucketName();

    String getContentType();

    LocalDateTime getCreatedAt();

    LocalDateTime getUpdatedAt();
}
