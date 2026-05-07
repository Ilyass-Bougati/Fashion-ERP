package com.sefault.server.stats.entity;

import com.sefault.server.stats.enums.PeriodType;
import jakarta.persistence.*;
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
public class FinancialStat {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private LocalDate statDate;

    @Enumerated(EnumType.STRING)
    private PeriodType periodType;

    private Double totalRevenue;
    private Double totalPayrollCost;
    private Double totalFixCharges;
    private Double grossProfit;
    private Double netProfit;
    private Double profitMarginPct;
    private Integer activeEmployeesCount;
    private Double revenuePerEmployee;

    @CreationTimestamp
    private LocalDateTime computedAt;

    private LocalDateTime reconciledAt;
}
