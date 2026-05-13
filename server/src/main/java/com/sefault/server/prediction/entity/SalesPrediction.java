package com.sefault.server.prediction.entity;

import com.sefault.server.stats.enums.PeriodType;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"target_date", "period_type", "model_version"}))
public class SalesPrediction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    private LocalDate targetDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    private PeriodType periodType;

    private Double predictedNetRevenue;
    private Double netRevenueLowerBound;
    private Double netRevenueUpperBound;

    private Integer predictedUnitsSold;
    private Integer unitsSoldLowerBound;
    private Integer unitsSoldUpperBound;

    private Integer predictedTransactions;
    private Integer transactionsLowerBound;
    private Integer transactionsUpperBound;

    @NotNull
    private String modelVersion;

    @CreationTimestamp
    private LocalDateTime predictedAt;
}
