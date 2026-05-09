package com.sefault.server.stats.dto.projection;

import com.sefault.server.stats.enums.PeriodType;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public interface FinancialStatProjection {
    UUID getId();

    LocalDate getStatDate();

    PeriodType getPeriodType();

    Double getTotalRevenue();

    Double getTotalPayrollCost();

    Double getTotalFixCharges();

    Double getGrossProfit();

    Double getNetProfit();

    Double getProfitMarginPct();

    Integer getActiveEmployeesCount();

    Double getRevenuePerEmployee();

    LocalDateTime getComputedAt();
}
