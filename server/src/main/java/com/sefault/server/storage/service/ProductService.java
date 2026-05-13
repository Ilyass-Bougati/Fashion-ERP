package com.sefault.server.storage.service;

import com.sefault.server.storage.dto.record.ProductRecord;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ProductService {
    Page<ProductRecord> getAll(Pageable pageable);

    ProductRecord getById(UUID id);

    ProductRecord save(ProductRecord record);

    ProductRecord update(UUID id, ProductRecord record);

    void delete(UUID id);
}
