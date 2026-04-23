package com.sefault.server.image.dto.record;

import com.sefault.server.hr.entity.Employee;
import com.sefault.server.storage.entity.Product;
import com.sefault.server.storage.entity.ProductVariation;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ImageRecord(
        UUID id,
        String objectKey,
        String bucketName,
        String contentType,
        List<Product> products,
        List<ProductVariation> productVariations,
        List<Employee> employees,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {}
