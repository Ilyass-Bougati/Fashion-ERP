package com.sefault.server.image.dto.projection;

import com.sefault.server.hr.entity.Employee;
import com.sefault.server.storage.entity.Product;
import com.sefault.server.storage.entity.ProductVariation;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ImageProjection {
    UUID getId();

    String getObjectKey();

    String getBucketName();

    String getContentType();

    List<Product> getProducts();

    List<ProductVariation> getProductVariations();

    List<Employee> getEmployees();

    LocalDateTime getCreatedAt();

    LocalDateTime getUpdatedAt();
}
