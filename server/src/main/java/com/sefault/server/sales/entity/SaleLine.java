package com.sefault.server.sales.entity;

import com.sefault.server.sales.entity.id.SaleLineId;
import com.sefault.server.storage.entity.ProductVariation;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.*;

@Entity
@NoArgsConstructor
@AllArgsConstructor
public class SaleLine {
    @EmbeddedId
    private SaleLineId id;

    @NotNull
    @Min(1)
    private Integer quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("saleId")
    private Sale sale;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("productVariationId")
    private ProductVariation productVariation;

    @NotNull
    @Positive
    private Double saleAtPrice;
}
