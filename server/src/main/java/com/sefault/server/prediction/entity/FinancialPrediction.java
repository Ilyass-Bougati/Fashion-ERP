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
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"target_date", "period_type", "model_version"}))
public class FinancialPrediction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    private LocalDate targetDate;

    @Enumerated(EnumType.STRING)
    private PeriodType periodType;

    private Double predictedTotalRevenue;
    private Double totalRevenueLowerBound;
    private Double totalRevenueUpperBound;

    private Double predictedNetProfit;
    private Double netProfitLowerBound;
    private Double netProfitUpperBound;

    @NotNull
    private String modelVersion;

    @CreationTimestamp
    private LocalDateTime predictedAt;
}
