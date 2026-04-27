package com.sefault.server.sales.dto.record;

import com.sefault.server.sales.entity.id.SaleLineId;
import java.util.UUID;

public record SaleLineRecord(
        SaleLineId id, Integer quantity, UUID saleId, UUID productVariationId, Double saleAtPrice) {}
