package com.sefault.server.sales.dto.record;

import com.sefault.server.sales.entity.Sale;
import com.sefault.server.sales.entity.id.SaleLineId;
import com.sefault.server.storage.entity.ProductVariation;

public record SaleLineRecord(
        SaleLineId id, Integer quantity, Sale sale, ProductVariation productVariation, Double saleAtPrice) {}
