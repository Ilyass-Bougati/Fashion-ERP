package com.sefault.server.storage.service;

import com.sefault.server.storage.dto.record.ProductRecord;
import java.util.UUID;

public interface ProductService {
    ProductRecord getById(UUID id);

    ProductRecord save(ProductRecord record);

    ProductRecord update(UUID id, ProductRecord record);

    void delete(UUID id);
}
