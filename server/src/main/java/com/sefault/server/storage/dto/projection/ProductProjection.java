package com.sefault.server.storage.dto.projection;

import com.sefault.server.image.entity.Image;
import com.sefault.server.storage.entity.ProductCategory;
import com.sefault.server.storage.entity.ProductVariation;
import com.sefault.server.storage.entity.Vendor;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface ProductProjection {
    UUID getId();

    String getName();

    ProductCategory getProductCategory();

    List<ProductVariation> getProductVariations();

    Image getImage();

    List<Vendor> getVendors();

    LocalDateTime getCreatedAt();

    LocalDateTime getUpdatedAt();
}
