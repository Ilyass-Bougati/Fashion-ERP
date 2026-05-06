package com.sefault.server.stats.dto.record;

import com.sefault.server.stats.enums.PeriodType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record EmployeePerformanceStatRecord(
        UUID id,
        @NotNull LocalDate statDate,
        @NotNull PeriodType periodType,
        @NotBlank String employeeCin,
        String employeeFullName,
        Integer salesCount,
        Double grossSalesAmount,
        Double commissionEarned,
        Integer itemsSold,
        Double avgDiscountGiven,
        LocalDateTime computedAt) {}
