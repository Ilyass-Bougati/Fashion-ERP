package com.sefault.server.stats.dto.projection;

import com.sefault.server.stats.enums.PeriodType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public interface EmployeePerformanceStatProjection {
    UUID getId();
    LocalDate getStatDate();
    PeriodType getPeriodType();
    String getEmployeeCin();
    String getEmployeeFullName();
    Integer getSalesCount();
    Double getGrossSalesAmount();
    Double getCommissionEarned();
    Integer getItemsSold();
    Double getAvgDiscountGiven();
    LocalDateTime getComputedAt();
}
