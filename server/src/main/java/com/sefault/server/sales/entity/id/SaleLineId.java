package com.sefault.server.sales.entity.id;

import jakarta.persistence.Embeddable;
import java.util.UUID;
import lombok.*;

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
