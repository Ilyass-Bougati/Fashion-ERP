package com.sefault.server.prediction.service.impl;

import com.sefault.server.prediction.client.PredictionClient;
import com.sefault.server.prediction.dto.api.BatchForecastRequest;
import com.sefault.server.prediction.dto.api.BatchForecastResponse;
import com.sefault.server.prediction.entity.StockPrediction;
import com.sefault.server.prediction.repository.StockPredictionRepository;
import com.sefault.server.prediction.service.StockPredictionService;
import com.sefault.server.stats.entity.StockStat;
import com.sefault.server.stats.enums.PeriodType;
import com.sefault.server.stats.repository.StockStatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StockPredictionServiceImpl implements StockPredictionService {
    private final StockStatRepository statRepo;
    private final StockPredictionRepository predRepo;
    private final PredictionClient client;

    @Transactional
    @Override
    public void generateStockForecasts() {
        // 1. Identify all SKUs tracked in stats
        List<String> allSkus = statRepo.findDistinctProductVariationSkus();

        List<List<Double>> batchPayload = new ArrayList<>();
        List<String> skuOrder = new ArrayList<>();

        for (String sku : allSkus) {
            List<StockStat> history = statRepo.findTop30ByProductVariationSkuOrderByStatDateDesc(sku);
            if (history.size() < 10) continue;

            List<Double> qtyHistory = new ArrayList<>(history.stream().map(s -> (double) s.getQuantityOnHand()).toList());
            Collections.reverse(qtyHistory);

            batchPayload.add(qtyHistory);
            skuOrder.add(sku);
        }

        if (batchPayload.isEmpty()) return;

        BatchForecastResponse response = client.fetchBatchForecast(new BatchForecastRequest(batchPayload, 7));

        for (int skuIdx = 0; skuIdx < skuOrder.size(); skuIdx++) {
            String sku = skuOrder.get(skuIdx);

            for (int day = 0; day < 7; day++) {
                LocalDate target = LocalDate.now().plusDays(day + 1);
                StockPrediction pred = predRepo
                        .findByTargetDateAndPeriodTypeAndProductVariationSkuAndModelVersion(target, PeriodType.DAILY, sku, response.model_version())
                        .orElse(new StockPrediction());

                pred.setTargetDate(target);
                pred.setPeriodType(PeriodType.DAILY);
                pred.setProductVariationSku(sku);
                pred.setPredictedQuantity(Math.max(0, response.predictions().get(skuIdx).get(day).intValue()));
                pred.setQuantityLowerBound(Math.max(0, response.lower_bounds().get(skuIdx).get(day).intValue()));
                pred.setQuantityUpperBound(Math.max(0, response.upper_bounds().get(skuIdx).get(day).intValue()));
                pred.setModelVersion(response.model_version());

                predRepo.save(pred);
            }
        }
    }
}
