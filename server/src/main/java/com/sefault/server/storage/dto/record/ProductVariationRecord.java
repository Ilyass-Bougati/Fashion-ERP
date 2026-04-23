package com.sefault.server.storage.dto.record;

import com.sefault.server.image.entity.Image;
import com.sefault.server.sales.entity.SaleLine;
import com.sefault.server.storage.entity.Product;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ProductVariationRecord(
        UUID id,
        String sku,
        Double price,
        List<SaleLine> saleLines,
        Product product,
        Integer quantity,
        Image image,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {}
