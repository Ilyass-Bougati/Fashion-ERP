package com.sefault.server.prediction.service.impl;

import com.sefault.server.prediction.client.PredictionClient;
import com.sefault.server.prediction.dto.api.BatchForecastRequest;
import com.sefault.server.prediction.dto.api.BatchForecastResponse;
import com.sefault.server.prediction.entity.EmployeePerformancePrediction;
import com.sefault.server.prediction.repository.EmployeePerformancePredictionRepository;
import com.sefault.server.prediction.service.EmployeePredictionService;
import com.sefault.server.stats.entity.EmployeePerformanceStat;
import com.sefault.server.stats.enums.PeriodType;
import com.sefault.server.stats.repository.EmployeePerformanceStatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EmployeePredictionServiceImpl implements EmployeePredictionService {
    private final EmployeePerformanceStatRepository statRepo;
    private final EmployeePerformancePredictionRepository predRepo;
    private final PredictionClient client;

    @Transactional
    @Override
    public void generateEmployeeForecasts() {
        List<String> activeCins = statRepo.findDistinctEmployeeCins();

        for (String cin : activeCins) {
            List<EmployeePerformanceStat> history = statRepo.findTop30ByEmployeeCinAndPeriodTypeOrderByStatDateDesc(cin, PeriodType.DAILY);
            if (history.size() < 10) continue;

            List<Double> salesHistory = history.stream().map(EmployeePerformanceStat::getGrossSalesAmount).toList();
            List<Double> chronological = new ArrayList<>(salesHistory);
            Collections.reverse(chronological);

            BatchForecastResponse response = client.fetchBatchForecast(new BatchForecastRequest(List.of(chronological), 7));

            for (int i = 0; i < 7; i++) {
                LocalDate target = LocalDate.now().plusDays(i + 1);
                EmployeePerformancePrediction pred = predRepo
                        .findByTargetDateAndPeriodTypeAndEmployeeCinAndModelVersion(target, PeriodType.DAILY, cin, response.model_version())
                        .orElse(new EmployeePerformancePrediction());

                pred.setTargetDate(target);
                pred.setPeriodType(PeriodType.DAILY);
                pred.setEmployeeCin(cin);
                pred.setEmployeeFullName(history.get(0).getEmployeeFullName());
                pred.setPredictedGrossSales(response.predictions().get(0).get(i));
                pred.setGrossSalesLowerBound(response.lower_bounds().get(0).get(i));
                pred.setGrossSalesUpperBound(response.upper_bounds().get(0).get(i));
                pred.setModelVersion(response.model_version());

                predRepo.save(pred);
            }
        }
    }
}
