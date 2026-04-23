package com.sefault.server.sales.dto.projection;

import com.sefault.server.sales.entity.Sale;
import com.sefault.server.sales.entity.id.SaleLineId;
import com.sefault.server.storage.entity.ProductVariation;

public interface SaleLineProjection {
    SaleLineId getId();

    Integer getQuantity();

    Sale getSale();

    ProductVariation getProductVariation();

    Double getSaleAtPrice();
}
