package com.sefault.server.stats.service.impl;

import com.sefault.server.sales.repository.SaleRepository;
import com.sefault.server.stats.dto.projection.RevenueAggregationProjection;
import com.sefault.server.stats.entity.SalesStat;
import com.sefault.server.stats.enums.PeriodType;
import com.sefault.server.stats.repository.SalesStatRepository;
import com.sefault.server.stats.service.SalesStatsService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class SalesStatsServiceImpl implements SalesStatsService {
    private final SaleRepository saleRepository;
    private final SalesStatRepository salesStatRepository;

    public void saveSalesStats(LocalDateTime start, LocalDateTime end, LocalDate anchorDate, PeriodType periodType) {
        Long totalTransactions = saleRepository.countValidTransactions(start, end);
        Long refundedCount = saleRepository.countRefundedTransactions(start, end);
        RevenueAggregationProjection revAgg = saleRepository.calculateRevenueAndUnits(start, end);

        String topCategory = saleRepository.findTopCategoryNameForPeriod(start, end);
        String topSku = saleRepository.findTopProductSkuForPeriod(start, end);

        Double grossRev = revAgg.getGrossRevenue();
        Double netRev = revAgg.getNetRevenue();
        Integer units = revAgg.getUnitsSold();

        Double totalDiscounts = grossRev - netRev;
        Double avgBasketValue = totalTransactions > 0 ? (netRev / totalTransactions) : 0.0;

        SalesStat existingStat = salesStatRepository
                .findByStatDateAndPeriodType(anchorDate, periodType)
                .orElse(null);

        SalesStat salesStat = SalesStat.builder()
                .id(existingStat != null ? existingStat.getId() : null)
                .computedAt(existingStat != null ? existingStat.getComputedAt() : null)
                .statDate(anchorDate)
                .periodType(periodType)
                .totalTransactions(totalTransactions.intValue())
                .refundedCount(refundedCount.intValue())
                .grossRevenue(grossRev)
                .netRevenue(netRev)
                .totalDiscounts(totalDiscounts)
                .avgBasketValue(avgBasketValue)
                .unitsSold(units)
                .topCategoryName(topCategory)
                .topProductSku(topSku)
                .reconciled(true)
                .reconciledAt(LocalDateTime.now())
                .build();

        salesStatRepository.save(salesStat);
    }
}
