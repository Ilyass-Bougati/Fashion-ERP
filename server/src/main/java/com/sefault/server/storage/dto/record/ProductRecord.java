package com.sefault.server.storage.dto.record;

import com.sefault.server.image.entity.Image;
import com.sefault.server.storage.entity.ProductCategory;
import com.sefault.server.storage.entity.ProductVariation;
import com.sefault.server.storage.entity.Vendor;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ProductRecord(
        UUID id,
        String name,
        ProductCategory productCategory,
        List<ProductVariation> productVariations,
        Image image,
        List<Vendor> vendors,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {}
