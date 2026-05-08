package com.sefault.server.stats.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import com.sefault.server.sales.repository.SaleRepository;
import com.sefault.server.stats.dto.projection.RevenueAggregationProjection;
import com.sefault.server.stats.entity.SalesStat;
import com.sefault.server.stats.enums.PeriodType;
import com.sefault.server.stats.repository.SalesStatRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SalesStatsServiceImplTest {

    @Mock
    private SaleRepository saleRepository;

    @Mock
    private SalesStatRepository salesStatRepository;

    @InjectMocks
    private SalesStatsServiceImpl service;

    @Test
    void saveSalesStats_CalculatesAggregationsCorrectly() {
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now();
        LocalDate anchor = LocalDate.now();

        RevenueAggregationProjection mockRev = mock(RevenueAggregationProjection.class);
        when(mockRev.getGrossRevenue()).thenReturn(1000.0);
        when(mockRev.getNetRevenue()).thenReturn(900.0);
        when(mockRev.getUnitsSold()).thenReturn(50);

        when(saleRepository.countValidTransactions(start, end)).thenReturn(10L);
        when(saleRepository.countRefundedTransactions(start, end)).thenReturn(2L);
        when(saleRepository.calculateRevenueAndUnits(start, end)).thenReturn(mockRev);
        when(saleRepository.findTopCategoryNameForPeriod(start, end)).thenReturn("Electronics");
        when(saleRepository.findTopProductSkuForPeriod(start, end)).thenReturn("SKU-123");
        when(salesStatRepository.findByStatDateAndPeriodType(anchor, PeriodType.DAILY))
                .thenReturn(Optional.empty());

        service.saveSalesStats(start, end, anchor, PeriodType.DAILY);

        ArgumentCaptor<SalesStat> captor = ArgumentCaptor.forClass(SalesStat.class);
        verify(salesStatRepository).save(captor.capture());

        SalesStat saved = captor.getValue();
        assertEquals(10, saved.getTotalTransactions());
        assertEquals(2, saved.getRefundedCount());
        assertEquals(1000.0, saved.getGrossRevenue());
        assertEquals(900.0, saved.getNetRevenue());
        assertEquals(100.0, saved.getTotalDiscounts()); // 1000 - 900
        assertEquals(90.0, saved.getAvgBasketValue()); // 900 / 10
        assertEquals("Electronics", saved.getTopCategoryName());
        assertEquals("SKU-123", saved.getTopProductSku());
    }
}
