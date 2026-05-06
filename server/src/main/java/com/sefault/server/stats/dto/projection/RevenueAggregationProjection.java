package com.sefault.server.stats.dto.projection;

public interface RevenueAggregationProjection {
    Double getGrossRevenue();
    Double getNetRevenue();
    Integer getUnitsSold();
}
