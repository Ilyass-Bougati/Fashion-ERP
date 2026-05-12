package com.sefault.server.prediction.dto.api;

import java.util.List;

public record BatchForecastRequest(
        List<List<Double>> historical_data,
        int horizon
) {}
