package com.sefault.server.storage.service;

import com.sefault.server.storage.dto.record.ProductCategoryRecord;
import java.util.UUID;

public interface ProductCategoryService {
    ProductCategoryRecord getById(UUID id);

    ProductCategoryRecord save(ProductCategoryRecord productCategory);

    ProductCategoryRecord update(UUID id, ProductCategoryRecord productCategory);

    void delete(UUID id);
}
