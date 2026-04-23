package com.sefault.server.sales.entity;

import com.sefault.server.sales.entity.id.SaleLineId;
import com.sefault.server.storage.entity.ProductVariation;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.*;

@Entity
@Getter
@Setter
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

    @Column(name = "sale_id", insertable = false, updatable = false)
    private UUID saleId;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("productVariationId")
    private ProductVariation productVariation;

    @Column(name = "product_variation_id", insertable = false, updatable = false)
    private UUID productVariationId;

    @NotNull
    @Positive
    private Double saleAtPrice;
}
