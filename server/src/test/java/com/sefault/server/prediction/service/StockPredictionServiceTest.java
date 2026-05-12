package com.sefault.server.prediction.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import com.sefault.server.prediction.client.PredictionClient;
import com.sefault.server.prediction.dto.api.BatchForecastRequest;
import com.sefault.server.prediction.dto.api.BatchForecastResponse;
import com.sefault.server.prediction.entity.StockPrediction;
import com.sefault.server.prediction.repository.StockPredictionRepository;
import com.sefault.server.prediction.service.impl.StockPredictionServiceImpl;
import com.sefault.server.stats.entity.StockStat;
import com.sefault.server.stats.repository.StockStatRepository;
import java.util.List;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StockPredictionServiceTest {

    @Mock
    private StockStatRepository statRepo;

    @Mock
    private StockPredictionRepository predRepo;

    @Mock
    private PredictionClient client;

    @InjectMocks
    private StockPredictionServiceImpl service;

    @Test
    void generateStockForecasts_BatchesMultipleSkusCorrectly() {
        // Arrange
        String skuA = "SKU-A";
        String skuB = "SKU-B";
        when(statRepo.findDistinctProductVariationSkus()).thenReturn(List.of(skuA, skuB));

        // Mock 30 days of history for each SKU
        // Ensure quantity is set to avoid NullPointerException in the service mapper
        when(statRepo.findTop30ByProductVariationSkuOrderByStatDateDesc(skuA)).thenReturn(createMockStockHistory(30));
        when(statRepo.findTop30ByProductVariationSkuOrderByStatDateDesc(skuB)).thenReturn(createMockStockHistory(30));

        // Mock API Response
        BatchForecastResponse mockResponse = new BatchForecastResponse(
                List.of(
                        List.of(10.0, 10.0, 10.0, 10.0, 10.0, 10.0, 10.0),
                        List.of(20.0, 20.0, 20.0, 20.0, 20.0, 20.0, 20.0)),
                List.of(List.of(5.0, 5.0, 5.0, 5.0, 5.0, 5.0, 5.0), List.of(15.0, 15.0, 15.0, 15.0, 15.0, 15.0, 15.0)),
                List.of(
                        List.of(15.0, 15.0, 15.0, 15.0, 15.0, 15.0, 15.0),
                        List.of(25.0, 25.0, 25.0, 25.0, 25.0, 25.0, 25.0)),
                "timesfm-2.5");
        when(client.fetchBatchForecast(any(BatchForecastRequest.class))).thenReturn(mockResponse);

        // Act
        service.generateStockForecasts();

        // Assert
        // Verify client call and count
        verify(client, times(1))
                .fetchBatchForecast(argThat(request -> request.historical_data() != null
                        && request.historical_data().size() == 2));

        // Verify save was called 14 times (7 days * 2 SKUs)
        verify(predRepo, times(14)).save(any(StockPrediction.class));
    }

    private List<StockStat> createMockStockHistory(int days) {
        return IntStream.range(0, days)
                .mapToObj(i -> {
                    StockStat stat = new StockStat();
                    stat.setQuantityOnHand(10); // CRITICAL: Fixes the NPE
                    return stat;
                })
                .toList();
    }
}
