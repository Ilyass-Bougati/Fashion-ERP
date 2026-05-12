package com.sefault.server.prediction.entity;

import com.sefault.server.stats.enums.PeriodType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"target_date", "period_type", "product_variation_sku", "model_version"}))
public class StockPrediction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    private LocalDate targetDate;

    @Enumerated(EnumType.STRING)
    private PeriodType periodType;

    @NotNull
    private String productVariationSku;
    private String productName;
    private String categoryName;

    private Integer predictedQuantity;
    private Integer quantityLowerBound;
    private Integer quantityUpperBound;

    @NotNull
    private String modelVersion;

    @CreationTimestamp
    private LocalDateTime predictedAt;
}
