package com.sefault.server.storage.dto.projection;

import java.time.LocalDateTime;
import java.util.UUID;

public interface ProductVariationProjection {
    UUID getId();

    String getSku();

    Double getPrice();

    UUID getProductId();

    Integer getQuantity();

    UUID getImageId();

    LocalDateTime getCreatedAt();

    LocalDateTime getUpdatedAt();
}
