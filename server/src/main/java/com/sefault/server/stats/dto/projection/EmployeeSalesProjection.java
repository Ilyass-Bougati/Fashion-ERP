package com.sefault.server.stats.dto.projection;

public interface EmployeeSalesProjection {
    String getCin();

    String getFirstName();

    String getLastName();

    Long getSalesCount();

    Double getGrossSalesAmount();

    Long getItemsSold();

    Double getAvgDiscountGiven();
}
