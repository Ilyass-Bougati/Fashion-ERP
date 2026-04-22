package com.sefault.server.sales.entity;

import com.sefault.server.sales.entity.id.SaleLineId;
import com.sefault.server.storage.entity.Product;
import com.sefault.server.storage.entity.ProductVariation;
import jakarta.persistence.*;
import jakarta.persistence.CascadeType;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.*;
import org.hibernate.annotations.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
