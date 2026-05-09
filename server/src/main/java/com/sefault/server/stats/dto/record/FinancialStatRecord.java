package com.sefault.server.stats.dto.record;

import com.sefault.server.stats.enums.PeriodType;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record FinancialStatRecord(
        UUID id,
        @NotNull LocalDate statDate,
        @NotNull PeriodType periodType,
        Double totalRevenue,
        Double totalPayrollCost,
        Double totalFixCharges,
        Double grossProfit,
        Double netProfit,
        Double profitMarginPct,
        Integer activeEmployeesCount,
        Double revenuePerEmployee,
        LocalDateTime computedAt) {}
