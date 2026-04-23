package com.sefault.server.storage.dto.projection;

import java.time.LocalDateTime;
import java.util.UUID;

public interface ProductProjection {
    UUID getId();

    String getName();

    UUID getProductCategoryId();

    UUID getImageId();

    LocalDateTime getCreatedAt();

    LocalDateTime getUpdatedAt();
}
