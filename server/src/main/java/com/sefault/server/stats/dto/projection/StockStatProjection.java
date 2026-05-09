package com.sefault.server.stats.dto.projection;

import com.sefault.server.stats.enums.PeriodType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public interface StockStatProjection {
    UUID getId();

    LocalDate getStatDate();

    PeriodType getPeriodType();

    String getProductVariationSku();

    String getProductName();

    String getCategoryName();

    Integer getQuantityOnHand();

    Double getTotalStockValue();

    Double getAvgDailyVelocity();

    Integer getDaysOfStockRemaining();

    Boolean getLowStockFlag();

    LocalDateTime getComputedAt();
}
