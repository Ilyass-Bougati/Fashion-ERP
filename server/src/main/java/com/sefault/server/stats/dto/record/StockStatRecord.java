package com.sefault.server.stats.dto.record;

import com.sefault.server.stats.enums.PeriodType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record StockStatRecord(
        UUID id,
        @NotNull LocalDate statDate,
        @NotNull PeriodType periodType,
        @NotBlank String productVariationSku,
        String productName,
        String categoryName,
        Integer quantityOnHand,
        Double totalStockValue,
        Double avgDailyVelocity,
        Integer daysOfStockRemaining,
        Boolean lowStockFlag,
        LocalDateTime computedAt
) {}
