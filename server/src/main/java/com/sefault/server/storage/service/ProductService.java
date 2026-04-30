package com.sefault.server.storage.service;

import com.sefault.server.storage.dto.projection.ProductProjection;
import com.sefault.server.storage.dto.record.ProductRecord;
import java.util.UUID;

public interface ProductService {
    ProductProjection getById(UUID id);

    ProductRecord save(ProductRecord product);

    ProductRecord update(ProductRecord product);

    void delete(UUID id);
}
