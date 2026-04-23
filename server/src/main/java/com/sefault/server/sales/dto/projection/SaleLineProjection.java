package com.sefault.server.sales.dto.projection;

import com.sefault.server.sales.entity.id.SaleLineId;
import java.util.UUID;

public interface SaleLineProjection {
    SaleLineId getId();

    Integer getQuantity();

    UUID getSaleId();

    UUID getProductVariationId();

    Double getSaleAtPrice();
}
