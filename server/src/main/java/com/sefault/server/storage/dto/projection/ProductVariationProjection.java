package com.sefault.server.storage.dto.projection;

import com.sefault.server.image.entity.Image;
import com.sefault.server.sales.entity.SaleLine;
import com.sefault.server.storage.entity.Product;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ProductVariationProjection {
    UUID getId();

    String getSku();

    Double getPrice();

    List<SaleLine> getSaleLines();

    Product getProduct();

    Integer getQuantity();

    Image getImage();

    LocalDateTime getCreatedAt();

    LocalDateTime getUpdatedAt();
}