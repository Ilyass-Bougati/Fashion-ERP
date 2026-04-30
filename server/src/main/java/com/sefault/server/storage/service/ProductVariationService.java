package com.sefault.server.storage.service;

import com.sefault.server.storage.dto.projection.ProductVariationProjection;
import com.sefault.server.storage.dto.record.ProductVariationRecord;
import java.util.UUID;

public interface ProductVariationService {
    ProductVariationProjection getById(UUID id);

    ProductVariationRecord save(ProductVariationRecord productVariation);

    ProductVariationRecord update(ProductVariationRecord productVariation);

    void delete(UUID id);
}
