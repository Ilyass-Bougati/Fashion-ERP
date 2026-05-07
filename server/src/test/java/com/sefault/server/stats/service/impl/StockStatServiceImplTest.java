package com.sefault.server.stats.service.impl;

import com.sefault.server.sales.repository.SaleRepository;
import com.sefault.server.stats.dto.projection.ProductVariationVelocityProjection;
import com.sefault.server.stats.entity.StockStat;
import com.sefault.server.stats.enums.PeriodType;
import com.sefault.server.stats.repository.StockStatRepository;
import com.sefault.server.storage.entity.Product;
import com.sefault.server.storage.entity.ProductCategory;
import com.sefault.server.storage.entity.ProductVariation;
import com.sefault.server.storage.repository.ProductVariationRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StockStatServiceImplTest {

    @Mock private ProductVariationRepository variationRepository;
    @Mock private SaleRepository saleRepository;
    @Mock private StockStatRepository stockStatRepository;

    @InjectMocks
    private StockStatServiceImpl service;

    @Test
    void saveStockStats_FlagsLowStockCorrectly() {
        LocalDate anchor = LocalDate.now();
        UUID pvId = UUID.randomUUID();

        // Setup a variation with low stock (velocity is high, quantity is low)
        ProductCategory cat = ProductCategory.builder().name("Category").build();
        Product prod = Product.builder().name("Product").productCategory(cat).build();
        ProductVariation pv = ProductVariation.builder()
                .id(pvId).sku("SKU-TEST").product(prod).quantity(10).price(5.0).build();

        ProductVariationVelocityProjection velProj = mock(ProductVariationVelocityProjection.class);
        when(velProj.getProductVariationId()).thenReturn(pvId);
        when(velProj.getUnitsSold()).thenReturn(60L); // 60 sold in 30 days = 2 per day

        when(saleRepository.getSalesVelocitySince(any())).thenReturn(List.of(velProj));
        when(variationRepository.findAllWithProductAndCategory()).thenReturn(List.of(pv));
        when(stockStatRepository.findByStatDateAndPeriodTypeAndProductVariationSku(anchor, PeriodType.DAILY, "SKU-TEST")).thenReturn(Optional.empty());

        service.saveStockStats(anchor, PeriodType.DAILY);

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<StockStat>> captor = ArgumentCaptor.forClass(List.class);
        verify(stockStatRepository).saveAll(captor.capture());

        StockStat saved = captor.getValue().get(0);
        assertEquals(50.0, saved.getTotalStockValue()); // 10 * 5.0
        assertEquals(2.0, saved.getAvgDailyVelocity()); // 60 / 30
        assertEquals(5, saved.getDaysOfStockRemaining()); // 10 qty / 2 daily
        assertTrue(saved.getLowStockFlag()); // 5 days is < 7, should be true
    }
}