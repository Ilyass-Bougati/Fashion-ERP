package com.sefault.server.storage.service;

import com.sefault.server.storage.dto.record.ProductVariationRecord;
import java.util.UUID;

public interface ProductVariationService {
    ProductVariationRecord getById(UUID id);

    ProductVariationRecord save(ProductVariationRecord record);

    ProductVariationRecord update(UUID id, ProductVariationRecord record);

    void delete(UUID id);
}
