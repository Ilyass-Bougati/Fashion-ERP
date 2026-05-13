package com.sefault.server.storage.service;

import com.sefault.server.storage.dto.record.ProductCategoryRecord;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductCategoryService {
    Page<ProductCategoryRecord> getAll(Pageable pageable);

    ProductCategoryRecord getById(UUID id);

    ProductCategoryRecord save(ProductCategoryRecord record);

    ProductCategoryRecord update(UUID id, ProductCategoryRecord record);

    void delete(UUID id);
}
