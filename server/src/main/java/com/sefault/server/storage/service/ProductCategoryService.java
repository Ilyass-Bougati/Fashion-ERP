package com.sefault.server.storage.service;

import com.sefault.server.storage.dto.projection.ProductCategoryProjection;
import com.sefault.server.storage.dto.record.ProductCategoryRecord;
import java.util.UUID;

public interface ProductCategoryService {
    ProductCategoryProjection getById(UUID id);

    ProductCategoryRecord save(ProductCategoryRecord productCategory);

    ProductCategoryRecord update(ProductCategoryRecord productCategory);

    void delete(UUID id);
}
