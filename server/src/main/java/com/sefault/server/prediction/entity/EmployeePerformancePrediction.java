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
@Table(
        uniqueConstraints =
                @UniqueConstraint(columnNames = {"target_date", "period_type", "employee_cin", "model_version"}))
public class EmployeePerformancePrediction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @NotNull
    private LocalDate targetDate;

    @Enumerated(EnumType.STRING)
    private PeriodType periodType;

    @NotNull
    private String employeeCin;

    private String employeeFullName;

    private Double predictedGrossSales;
    private Double grossSalesLowerBound;
    private Double grossSalesUpperBound;

    @NotNull
    private String modelVersion;

    @CreationTimestamp
    private LocalDateTime predictedAt;
}
