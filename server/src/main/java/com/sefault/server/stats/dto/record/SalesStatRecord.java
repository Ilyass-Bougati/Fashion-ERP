package com.sefault.server.stats.dto.record;

import com.sefault.server.stats.enums.PeriodType;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record SalesStatRecord(
        UUID id,
        @NotNull LocalDate statDate,
        @NotNull PeriodType periodType,
        Integer totalTransactions,
        Integer refundedCount,
        Double grossRevenue,
        Double netRevenue,
        Double totalDiscounts,
        Double avgBasketValue,
        Integer unitsSold,
        String topCategoryName,
        String topProductSku,
        Boolean reconciled,
        LocalDateTime computedAt,
        LocalDateTime reconciledAt) {}
