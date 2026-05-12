package com.sefault.server.prediction.client;

import com.sefault.server.prediction.dto.api.BatchForecastRequest;
import com.sefault.server.prediction.dto.api.BatchForecastResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.io.IOException;

@Slf4j
@Service
@RequiredArgsConstructor
public class PredictionClient {

    private final RestClient predictionRestClient;

    public BatchForecastResponse fetchBatchForecast(BatchForecastRequest request) {
        log.info("Sending forecast request to Python API... (Horizon: {} days, Batches: {})",
                request.horizon(), request.historical_data().size());

        return predictionRestClient.post()
                .uri("/api/forecast/series")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    String errorBody = getErrorBody(res);
                    log.error("Prediction API Error [{}]: {}", res.getStatusCode(), errorBody);
                    throw new RuntimeException("Failed to fetch forecast from Prediction Microservice: " + res.getStatusCode());
                })
                .body(BatchForecastResponse.class);
    }

    private String getErrorBody(ClientHttpResponse res) {
        try {
            return new String(res.getBody().readAllBytes());
        } catch (IOException e) {
            return "Unable to read error body";
        }
    }
}