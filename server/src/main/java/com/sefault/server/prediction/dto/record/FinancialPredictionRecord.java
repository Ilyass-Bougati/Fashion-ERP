package com.sefault.server.prediction.dto.record;

import com.sefault.server.stats.enums.PeriodType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record FinancialPredictionRecord(
        UUID id,
        @NotNull LocalDate targetDate,
        @NotNull PeriodType periodType,
        Double predictedTotalRevenue,
        Double totalRevenueLowerBound,
        Double totalRevenueUpperBound,
        Double predictedNetProfit,
        Double netProfitLowerBound,
        Double netProfitUpperBound,
        @NotBlank String modelVersion,
        LocalDateTime predictedAt) {}