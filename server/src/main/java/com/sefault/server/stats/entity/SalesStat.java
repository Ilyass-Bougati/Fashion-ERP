package com.sefault.server.stats.entity;

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
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"stat_date", "period_type"}))
public class SalesStat {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private LocalDate statDate;

    @NotNull
    @Enumerated(EnumType.STRING)
    private PeriodType periodType;

    private Integer totalTransactions;
    private Integer refundedCount;
    private Double grossRevenue;
    private Double netRevenue;
    private Double totalDiscounts;
    private Double avgBasketValue;
    private Integer unitsSold;
    private String topCategoryName;
    private String topProductSku;
    private Boolean reconciled;

    @CreationTimestamp
    private LocalDateTime computedAt;

    private LocalDateTime reconciledAt;
}
