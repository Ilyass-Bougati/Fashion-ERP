package com.sefault.server.storage.dto.record;

import com.sefault.server.storage.entity.Product;

import java.util.List;
import java.util.UUID;

public record ProductCategoryRecord(
        UUID id,
        String name,
        List<Product> products,
        String description
) {
}

