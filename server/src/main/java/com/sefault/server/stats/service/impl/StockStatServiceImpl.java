package com.sefault.server.stats.service.impl;

import com.sefault.server.sales.repository.SaleRepository;
import com.sefault.server.stats.dto.projection.ProductVariationVelocityProjection;
import com.sefault.server.stats.entity.StockStat;
import com.sefault.server.stats.enums.PeriodType;
import com.sefault.server.stats.repository.StockStatRepository;
import com.sefault.server.stats.service.StockStatService;
import com.sefault.server.storage.entity.ProductVariation;
import com.sefault.server.storage.repository.ProductVariationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class StockStatServiceImpl implements StockStatService {
    private final ProductVariationRepository productVariationRepository;
    private final SaleRepository saleRepository;
    private final StockStatRepository stockStatRepository;

    public void saveStockStats(LocalDate anchorDate, PeriodType periodType) {
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);

        List<ProductVariationVelocityProjection> velocities = saleRepository.getSalesVelocitySince(thirtyDaysAgo);
        Map<UUID, Long> velocityMap = velocities.stream()
                .collect(Collectors.toMap(
                        ProductVariationVelocityProjection::getProductVariationId,
                        ProductVariationVelocityProjection::getUnitsSold
                ));

        List<ProductVariation> allVariations = productVariationRepository.findAllWithProductAndCategory();
        List<StockStat> statsToSave = new ArrayList<>();

        for (ProductVariation pv : allVariations) {

            Long unitsSold30Days = velocityMap.getOrDefault(pv.getId(), 0L);
            Double avgDailyVelocity = unitsSold30Days / 30.0;

            Integer daysRemaining = null;
            if (avgDailyVelocity > 0) {
                daysRemaining = (int) (pv.getQuantity() / avgDailyVelocity);
            }

            Boolean isLowStock = (daysRemaining != null && daysRemaining < 7) || pv.getQuantity() == 0;
            Double totalStockValue = pv.getQuantity() * pv.getPrice();

            StockStat existingStat = stockStatRepository
                    .findByStatDateAndPeriodTypeAndProductVariationSku(anchorDate, periodType, pv.getSku())
                    .orElse(null);

            StockStat stat = StockStat.builder()
                    .id(existingStat != null ? existingStat.getId() : null)
                    .computedAt(existingStat != null ? existingStat.getComputedAt() : null)
                    .statDate(anchorDate)
                    .periodType(periodType)
                    .productVariationSku(pv.getSku())
                    .productName(pv.getProduct().getName())
                    .categoryName(pv.getProduct().getProductCategory().getName())
                    .quantityOnHand(pv.getQuantity())
                    .totalStockValue(totalStockValue)
                    .avgDailyVelocity(avgDailyVelocity)
                    .daysOfStockRemaining(daysRemaining)
                    .lowStockFlag(isLowStock)
                    .reconciledAt(LocalDateTime.now())
                    .build();

            statsToSave.add(stat);
        }

        stockStatRepository.saveAll(statsToSave);
    }
}
