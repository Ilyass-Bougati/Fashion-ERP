package com.sefault.server.prediction.dto.record;

import com.sefault.server.stats.enums.PeriodType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record EmployeePerformancePredictionRecord(
        UUID id,
        @NotNull LocalDate targetDate,
        @NotNull PeriodType periodType,
        @NotBlank String employeeCin,
        String employeeFullName,
        Double predictedGrossSales,
        Double grossSalesLowerBound,
        Double grossSalesUpperBound,
        @NotBlank String modelVersion,
        LocalDateTime predictedAt) {}
