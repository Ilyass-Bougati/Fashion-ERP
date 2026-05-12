package com.sefault.server.prediction.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class PredictionClientConfig {

    @Value("${erp.prediction.service.url:http://localhost:8000}")
    private String predictionServiceUrl;

    @Bean
    public RestClient predictionRestClient(RestClient.Builder builder) {
        return builder
                .baseUrl(predictionServiceUrl)
                .build();
    }
}