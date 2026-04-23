package com.sefault.server.storage.dto.projection;

import com.sefault.server.storage.entity.Product;

import java.util.List;
import java.util.UUID;

public interface ProductCategoryProjection {
    UUID getId();

    String getName();

    List<Product> getProducts();

    String getDescription();
}