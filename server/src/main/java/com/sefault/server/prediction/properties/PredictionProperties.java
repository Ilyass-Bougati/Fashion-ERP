package com.sefault.server.prediction.properties;


import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "erp.prediction")
public record PredictionProperties(
        String serviceUrl
) {}