package com.sefault.server.prediction.service;

import com.sefault.server.prediction.client.PredictionClient;
import com.sefault.server.prediction.dto.api.BatchForecastRequest;
import com.sefault.server.prediction.dto.api.BatchForecastResponse;
import com.sefault.server.prediction.entity.FinancialPrediction;
import com.sefault.server.prediction.repository.FinancialPredictionRepository;
import com.sefault.server.stats.entity.FinancialStat;
import com.sefault.server.stats.enums.PeriodType;
import com.sefault.server.stats.repository.FinancialStatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FinancialPredictionService {
    private final FinancialStatRepository financialStatRepository;
    private final FinancialPredictionRepository financialPredictionRepository;
    private final PredictionClient predictionClient;

    @Transactional
    public void generateMonthlyFinancialForecast() {
        List<FinancialStat> history = financialStatRepository.findTop24ByPeriodTypeOrderByStatDateDesc(PeriodType.MONTHLY);
        if (history.size() < 10) return;

        List<FinancialStat> chronological = new ArrayList<>(history);
        Collections.reverse(chronological);

        List<Double> revenueHistory = chronological.stream().map(FinancialStat::getTotalRevenue).toList();
        List<Double> profitHistory = chronological.stream().map(FinancialStat::getNetProfit).toList();

        BatchForecastResponse response = predictionClient.fetchBatchForecast(
                new BatchForecastRequest(List.of(revenueHistory, profitHistory), 3)
        );

        for (int i = 0; i < 3; i++) {
            LocalDate targetDate = LocalDate.now().plusMonths(i + 1).withDayOfMonth(1);
            FinancialPrediction pred = financialPredictionRepository
                    .findByTargetDateAndPeriodTypeAndModelVersion(targetDate, PeriodType.MONTHLY, response.model_version())
                    .orElse(new FinancialPrediction());

            pred.setTargetDate(targetDate);
            pred.setPeriodType(PeriodType.MONTHLY);
            pred.setModelVersion(response.model_version());

            pred.setPredictedTotalRevenue(response.predictions().get(0).get(i));
            pred.setTotalRevenueLowerBound(response.lower_bounds().get(0).get(i));
            pred.setTotalRevenueUpperBound(response.upper_bounds().get(0).get(i));

            pred.setPredictedNetProfit(response.predictions().get(1).get(i));
            pred.setNetProfitLowerBound(response.lower_bounds().get(1).get(i));
            pred.setNetProfitUpperBound(response.upper_bounds().get(1).get(i));

            financialPredictionRepository.save(pred);
        }
    }
}