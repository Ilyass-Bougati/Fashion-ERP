package com.sefault.server.storage.service;

import com.sefault.server.storage.dto.record.ProductVariationRecord;
import java.util.UUID;

public interface ProductVariationService {
    ProductVariationRecord getById(UUID id);

    ProductVariationRecord save(ProductVariationRecord productVariation);

    ProductVariationRecord update(UUID id, ProductVariationRecord productVariation);

    void delete(UUID id);
}
