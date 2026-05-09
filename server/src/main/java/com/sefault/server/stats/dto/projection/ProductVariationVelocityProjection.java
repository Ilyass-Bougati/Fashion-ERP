package com.sefault.server.stats.dto.projection;

import java.util.UUID;

public interface ProductVariationVelocityProjection {
    UUID getProductVariationId();

    Long getUnitsSold();
}
