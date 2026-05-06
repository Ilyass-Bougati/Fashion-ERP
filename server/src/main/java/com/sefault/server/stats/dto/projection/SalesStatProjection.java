package com.sefault.server.stats.dto.projection;

import com.sefault.server.stats.enums.PeriodType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public interface SalesStatProjection {
    UUID getId();

    LocalDate getStatDate();

    PeriodType getPeriodType();

    Integer getTotalTransactions();

    Integer getRefundedCount();

    Double getGrossRevenue();

    Double getNetRevenue();

    Double getTotalDiscounts();

    Double getAvgBasketValue();

    Integer getUnitsSold();

    String getTopCategoryName();

    String getTopProductSku();

    Boolean getReconciled();

    LocalDateTime getComputedAt();

    LocalDateTime getReconciledAt();
}
