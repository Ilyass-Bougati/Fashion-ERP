package com.sefault.server.stats.entity;

import com.sefault.server.stats.enums.PeriodType;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"stat_date", "period_type"}))
public class StockStat {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private LocalDate statDate;

    @Enumerated(EnumType.STRING)
    private PeriodType periodType;

    private String productVariationSku;
    private String productName;
    private String categoryName;
    private Integer quantityOnHand;
    private Double totalStockValue;
    private Double avgDailyVelocity;
    private Integer daysOfStockRemaining;
    private Boolean lowStockFlag;

    @CreationTimestamp
    private LocalDateTime computedAt;
}
