package com.sefault.server.prediction.dto.api;

import java.util.List;

public record BatchForecastResponse(
        List<List<Double>> predictions,
        List<List<Double>> lower_bounds,
        List<List<Double>> upper_bounds,
        String model_version) {}
