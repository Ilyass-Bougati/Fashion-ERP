package com.sefault.server.storage.service;

import com.sefault.server.storage.dto.record.ProductVariationRecord;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductVariationService {
    Page<ProductVariationRecord> getAll(Pageable pageable);

    ProductVariationRecord getById(UUID id);

    ProductVariationRecord save(ProductVariationRecord record);

    ProductVariationRecord update(UUID id, ProductVariationRecord record);

    void delete(UUID id);
}
