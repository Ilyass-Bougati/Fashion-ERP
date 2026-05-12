package com.sefault.server.prediction.service;

import com.sefault.server.prediction.client.PredictionClient;
import com.sefault.server.prediction.dto.api.BatchForecastResponse;
import com.sefault.server.prediction.repository.FinancialPredictionRepository;
import com.sefault.server.prediction.service.impl.FinancialPredictionServiceImpl;
import com.sefault.server.stats.entity.FinancialStat;
import com.sefault.server.stats.enums.PeriodType;
import com.sefault.server.stats.repository.FinancialStatRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FinancialPredictionServiceTest {

    @Mock private FinancialStatRepository statRepo;
    @Mock private FinancialPredictionRepository predRepo;
    @Mock private PredictionClient client;

    @InjectMocks
    private FinancialPredictionServiceImpl service;

    @Test
    void generateMonthlyFinancialForecast_SavesCorrectHorizon() {
        // Arrange
        when(statRepo.findTop24ByPeriodTypeOrderByStatDateDesc(PeriodType.MONTHLY))
                .thenReturn(createMockFinancialHistory(12));

        BatchForecastResponse mockResponse = new BatchForecastResponse(
                List.of(List.of(1000.0, 1100.0, 1200.0), List.of(500.0, 550.0, 600.0)),
                List.of(List.of(900.0, 900.0, 900.0), List.of(400.0, 400.0, 400.0)),
                List.of(List.of(1200.0, 1200.0, 1200.0), List.of(700.0, 700.0, 700.0)),
                "timesfm-2.5"
        );
        when(client.fetchBatchForecast(any())).thenReturn(mockResponse);

        // Act
        service.generateMonthlyFinancialForecast();

        // Assert
        verify(predRepo, times(3)).save(any()); // Horizon was set to 3 in service
    }

    private List<FinancialStat> createMockFinancialHistory(int count) {
        List<FinancialStat> list = new java.util.ArrayList<>();
        for(int i=0; i<count; i++) list.add(new FinancialStat());
        return list;
    }
}