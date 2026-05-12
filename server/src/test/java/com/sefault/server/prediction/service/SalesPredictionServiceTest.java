package com.sefault.server.prediction.service;

import com.sefault.server.prediction.client.PredictionClient;
import com.sefault.server.prediction.dto.api.BatchForecastRequest;
import com.sefault.server.prediction.dto.api.BatchForecastResponse;
import com.sefault.server.prediction.entity.SalesPrediction;
import com.sefault.server.prediction.repository.SalesPredictionRepository;
import com.sefault.server.prediction.service.impl.SalesPredictionServiceImpl;
import com.sefault.server.stats.entity.SalesStat;
import com.sefault.server.stats.enums.PeriodType;
import com.sefault.server.stats.repository.SalesStatRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SalesPredictionServiceTest {

    @Mock private SalesStatRepository salesStatRepository;
    @Mock private SalesPredictionRepository salesPredictionRepository;
    @Mock private PredictionClient predictionClient;

    @InjectMocks
    private SalesPredictionServiceImpl service;

    @Test
    void generateDailySalesForecast_AbortsIfLessThan10DaysData() {
        List<SalesStat> shortHistory = createMockHistory(5);
        when(salesStatRepository.findTop60ByPeriodTypeOrderByStatDateDesc(PeriodType.DAILY)).thenReturn(shortHistory);

        service.generateDailySalesForecast();

        verify(predictionClient, never()).fetchBatchForecast(any());
        verify(salesPredictionRepository, never()).saveAll(any());
    }

    @Test
    void generateDailySalesForecast_AbortsGracefullyOnApiError() {
        List<SalesStat> history = createMockHistory(15);
        when(salesStatRepository.findTop60ByPeriodTypeOrderByStatDateDesc(PeriodType.DAILY)).thenReturn(history);

        when(predictionClient.fetchBatchForecast(any())).thenThrow(new RuntimeException("API Down"));

        service.generateDailySalesForecast();

        verify(salesPredictionRepository, never()).saveAll(any());
    }

    @Test
    void generateDailySalesForecast_Success_SavesPredictions() {
        List<SalesStat> history = createMockHistory(12);
        when(salesStatRepository.findTop60ByPeriodTypeOrderByStatDateDesc(PeriodType.DAILY)).thenReturn(history);

        BatchForecastResponse mockResponse = createMockResponse();
        when(predictionClient.fetchBatchForecast(any())).thenReturn(mockResponse);

        when(salesPredictionRepository.findByTargetDateAndPeriodTypeAndModelVersion(any(), any(), any()))
                .thenReturn(Optional.empty());

        service.generateDailySalesForecast();

        ArgumentCaptor<BatchForecastRequest> reqCaptor = ArgumentCaptor.forClass(BatchForecastRequest.class);
        verify(predictionClient).fetchBatchForecast(reqCaptor.capture());

        BatchForecastRequest sentRequest = reqCaptor.getValue();
        assertEquals(7, sentRequest.horizon());
        assertEquals(3, sentRequest.historical_data().size());
        assertEquals(12, sentRequest.historical_data().get(0).size());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<SalesPrediction>> saveCaptor = ArgumentCaptor.forClass(List.class);
        verify(salesPredictionRepository).saveAll(saveCaptor.capture());

        List<SalesPrediction> savedPredictions = saveCaptor.getValue();
        assertEquals(7, savedPredictions.size());

        SalesPrediction day1 = savedPredictions.get(0);
        assertEquals(LocalDate.now().plusDays(1), day1.getTargetDate());
        assertEquals("timesfm-2.5", day1.getModelVersion());

        assertEquals(1000.0, day1.getPredictedNetRevenue());
        assertEquals(900.0, day1.getNetRevenueLowerBound());
        assertEquals(1100.0, day1.getNetRevenueUpperBound());

        assertEquals(0, day1.getPredictedUnitsSold());
    }


    private List<SalesStat> createMockHistory(int days) {
        List<SalesStat> list = new ArrayList<>();
        for (int i = 0; i < days; i++) {
            SalesStat stat = new SalesStat();
            stat.setStatDate(LocalDate.now().minusDays(i));
            stat.setNetRevenue(100.0 * (i + 1));
            stat.setUnitsSold(10 * (i + 1));
            stat.setTotalTransactions(2 * (i + 1));
            list.add(stat);
        }
        return list;
    }

    private BatchForecastResponse createMockResponse() {
        List<Double> revPred = List.of(1000.0, 1010.0, 1020.0, 1030.0, 1040.0, 1050.0, 1060.0);
        List<Double> revLow = List.of(900.0, 900.0, 900.0, 900.0, 900.0, 900.0, 900.0);
        List<Double> revHigh = List.of(1100.0, 1100.0, 1100.0, 1100.0, 1100.0, 1100.0, 1100.0);

        List<Double> unitPred = List.of(-5.0, 50.0, 50.0, 50.0, 50.0, 50.0, 50.0);
        List<Double> unitLow = List.of(-10.0, 40.0, 40.0, 40.0, 40.0, 40.0, 40.0);
        List<Double> unitHigh = List.of(10.0, 60.0, 60.0, 60.0, 60.0, 60.0, 60.0);

        List<Double> transPred = List.of(10.0, 10.0, 10.0, 10.0, 10.0, 10.0, 10.0);
        List<Double> transLow = List.of(5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0);
        List<Double> transHigh = List.of(15.0, 15.0, 15.0, 15.0, 15.0, 15.0, 15.0);

        return new BatchForecastResponse(
                List.of(revPred, unitPred, transPred),
                List.of(revLow, unitLow, transLow),
                List.of(revHigh, unitHigh, transHigh),
                "timesfm-2.5"
        );
    }
}