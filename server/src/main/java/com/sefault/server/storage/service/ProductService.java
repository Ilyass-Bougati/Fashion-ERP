package com.sefault.server.storage.service;

import com.sefault.server.storage.dto.record.ProductRecord;
import java.util.UUID;

public interface ProductService {
    ProductRecord getById(UUID id);

    ProductRecord save(ProductRecord product);

    ProductRecord update(UUID id, ProductRecord product);

    void delete(UUID id);
}
