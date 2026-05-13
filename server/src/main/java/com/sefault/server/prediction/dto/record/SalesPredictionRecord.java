package com.sefault.server.prediction.dto.record;

import com.sefault.server.stats.enums.PeriodType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record SalesPredictionRecord(
        UUID id,
        @NotNull LocalDate targetDate,
        @NotNull PeriodType periodType,
        Double predictedNetRevenue,
        Double netRevenueLowerBound,
        Double netRevenueUpperBound,
        Integer predictedUnitsSold,
        Integer unitsSoldLowerBound,
        Integer unitsSoldUpperBound,
        Integer predictedTransactions,
        Integer transactionsLowerBound,
        Integer transactionsUpperBound,
        @NotBlank String modelVersion,
        LocalDateTime predictedAt) {}
