package com.sefault.server.sales.entity.id;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@Embeddable
@EqualsAndHashCode
@AllArgsConstructor
@NoArgsConstructor
public class SaleLineId {
    private UUID saleId;
    private UUID productVariationId;
}
