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
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"stat_date", "period_type", "employee_cin"}))
public class EmployeePerformanceStat {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private LocalDate statDate;

    @Enumerated(EnumType.STRING)
    private PeriodType periodType;

    private String employeeCin;
    private String employeeFullName;
    private Integer salesCount;
    private Double grossSalesAmount;
    private Double commissionEarned;
    private Integer itemsSold;
    private Double avgDiscountGiven;

    @CreationTimestamp
    private LocalDateTime computedAt;
}
