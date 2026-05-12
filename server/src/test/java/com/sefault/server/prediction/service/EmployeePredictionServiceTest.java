package com.sefault.server.prediction.service;

import com.sefault.server.prediction.client.PredictionClient;
import com.sefault.server.prediction.dto.api.BatchForecastResponse;
import com.sefault.server.prediction.repository.EmployeePerformancePredictionRepository;
import com.sefault.server.stats.entity.EmployeePerformanceStat;
import com.sefault.server.stats.repository.EmployeePerformanceStatRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmployeePredictionServiceTest {

    @Mock private EmployeePerformanceStatRepository statRepo;
    @Mock private EmployeePerformancePredictionRepository predRepo;
    @Mock private PredictionClient client;

    @InjectMocks
    private EmployeePredictionService service;

    @Test
    void generateEmployeeForecasts_IteratesAllActiveEmployees() {
        // Arrange
        String cin1 = "CIN-1";
        String cin2 = "CIN-2";
        when(statRepo.findDistinctEmployeeCins()).thenReturn(List.of(cin1, cin2));

        EmployeePerformanceStat mockStat = new EmployeePerformanceStat();
        mockStat.setEmployeeFullName("John Doe");

        when(statRepo.findTop30ByEmployeeCinAndPeriodTypeOrderByStatDateDesc(anyString(), any()))
                .thenReturn(List.of(mockStat, mockStat, mockStat, mockStat, mockStat,
                        mockStat, mockStat, mockStat, mockStat, mockStat)); // 10 days

        BatchForecastResponse mockResponse = new BatchForecastResponse(
                List.of(List.of(100.0, 100.0, 100.0, 100.0, 100.0, 100.0, 100.0)),
                List.of(List.of(80.0, 80.0, 80.0, 80.0, 80.0, 80.0, 80.0)),
                List.of(List.of(120.0, 120.0, 120.0, 120.0, 120.0, 120.0, 120.0)),
                "timesfm-2.5"
        );
        when(client.fetchBatchForecast(any())).thenReturn(mockResponse);

        // Act
        service.generateEmployeeForecasts();

        // Assert
        verify(client, times(2)).fetchBatchForecast(any()); // One call per employee
        verify(predRepo, times(14)).save(any()); // 7 days * 2 employees
    }
}