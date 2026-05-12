package com.sefault.server.prediction.client;

import com.sefault.server.prediction.dto.api.BatchForecastRequest;
import com.sefault.server.prediction.dto.api.BatchForecastResponse;
import com.sefault.server.prediction.properties.PredictionProperties;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Slf4j
@Service
public class PredictionClient {

    private final RestClient restClient;

    public PredictionClient(RestClient.Builder builder, PredictionProperties properties) {
        String baseUrl = properties.serviceUrl() != null ? properties.serviceUrl() : "http://localhost:8000";
        this.restClient = builder.baseUrl(baseUrl).build();
    }

    public BatchForecastResponse fetchBatchForecast(BatchForecastRequest request) {
        log.info(
                "Sending forecast request to Python API... (Horizon: {} days, Batches: {})",
                request.horizon(),
                request.historical_data().size());

        return restClient
                .post()
                .uri("/api/forecast/series")
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .onStatus(HttpStatusCode::isError, (req, res) -> {
                    String errorBody = getErrorBody(res);
                    log.error("Prediction API Error [{}]: {}", res.getStatusCode(), errorBody);
                    throw new RuntimeException(
                            "Failed to fetch forecast from Prediction Microservice: " + res.getStatusCode());
                })
                .body(BatchForecastResponse.class);
    }

    private String getErrorBody(org.springframework.http.client.ClientHttpResponse res) {
        try {
            return new String(res.getBody().readAllBytes());
        } catch (IOException e) {
            return "Unable to read error body";
        }
    }
}
