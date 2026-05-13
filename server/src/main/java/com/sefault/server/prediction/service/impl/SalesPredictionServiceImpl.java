package com.sefault.server.prediction.service.impl;

import com.sefault.server.prediction.client.PredictionClient;
import com.sefault.server.prediction.dto.api.BatchForecastRequest;
import com.sefault.server.prediction.dto.api.BatchForecastResponse;
import com.sefault.server.prediction.entity.SalesPrediction;
import com.sefault.server.prediction.repository.SalesPredictionRepository;
import com.sefault.server.prediction.service.SalesPredictionService;
import com.sefault.server.stats.entity.SalesStat;
import com.sefault.server.stats.enums.PeriodType;
import com.sefault.server.stats.repository.SalesStatRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SalesPredictionServiceImpl implements SalesPredictionService {

    private final SalesStatRepository salesStatRepository;
    private final SalesPredictionRepository salesPredictionRepository;
    private final PredictionClient predictionClient;

    private static final int HORIZON_DAYS = 7;

    @Transactional
    @Override
    public void generateDailySalesForecast() {
        log.info("Starting Daily Sales Forecast Generation...");

        List<SalesStat> history = salesStatRepository.findTop60ByPeriodTypeOrderByStatDateDesc(PeriodType.DAILY);

        List<SalesStat> chronologicalHistory = new ArrayList<>(history);
        Collections.reverse(chronologicalHistory);

        if (chronologicalHistory.size() < 10) {
            log.warn("Not enough historical data to generate a forecast. Found: {}", chronologicalHistory.size());
            return;
        }

        List<Double> revenueHistory =
                chronologicalHistory.stream().map(SalesStat::getNetRevenue).toList();
        List<Double> unitsHistory = chronologicalHistory.stream()
                .map(s -> (double) s.getUnitsSold())
                .toList();
        List<Double> transactionsHistory = chronologicalHistory.stream()
                .map(s -> (double) s.getTotalTransactions())
                .toList();

        List<List<Double>> batchData = List.of(revenueHistory, unitsHistory, transactionsHistory);
        BatchForecastRequest request = new BatchForecastRequest(batchData, HORIZON_DAYS);

        BatchForecastResponse response;
        try {
            response = predictionClient.fetchBatchForecast(request);
        } catch (Exception e) {
            log.error("Aborting forecast generation due to API failure.");
            return;
        }

        LocalDate today = LocalDate.now();
        List<SalesPrediction> predictionsToSave = new ArrayList<>();

        for (int i = 0; i < HORIZON_DAYS; i++) {
            LocalDate targetDate = today.plusDays(i + 1);

            Double predRevenue = response.predictions().get(0).get(i);
            Double lowerRevenue = response.lower_bounds().get(0).get(i);
            Double upperRevenue = response.upper_bounds().get(0).get(i);

            Double predUnits = response.predictions().get(1).get(i);
            Double lowerUnits = response.lower_bounds().get(1).get(i);
            Double upperUnits = response.upper_bounds().get(1).get(i);

            Double predTrans = response.predictions().get(2).get(i);
            Double lowerTrans = response.lower_bounds().get(2).get(i);
            Double upperTrans = response.upper_bounds().get(2).get(i);

            SalesPrediction existing = salesPredictionRepository
                    .findByTargetDateAndPeriodTypeAndModelVersion(
                            targetDate, PeriodType.DAILY, response.model_version())
                    .orElse(new SalesPrediction());

            existing.setTargetDate(targetDate);
            existing.setPeriodType(PeriodType.DAILY);
            existing.setModelVersion(response.model_version());

            existing.setPredictedNetRevenue(predRevenue);
            existing.setNetRevenueLowerBound(lowerRevenue);
            existing.setNetRevenueUpperBound(upperRevenue);

            existing.setPredictedUnitsSold(Math.max(0, predUnits.intValue()));
            existing.setUnitsSoldLowerBound(Math.max(0, lowerUnits.intValue()));
            existing.setUnitsSoldUpperBound(Math.max(0, upperUnits.intValue()));

            existing.setPredictedTransactions(Math.max(0, predTrans.intValue()));
            existing.setTransactionsLowerBound(Math.max(0, lowerTrans.intValue()));
            existing.setTransactionsUpperBound(Math.max(0, upperTrans.intValue()));

            predictionsToSave.add(existing);
        }

        salesPredictionRepository.saveAll(predictionsToSave);
        log.info("Successfully generated and saved {} days of Sales Predictions!", HORIZON_DAYS);
    }
}
