package com.sefault.server.prediction.controller;

import com.sefault.server.prediction.scheduler.PredictionCronScheduler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/v1/test/predictions")
@RequiredArgsConstructor
public class TestPredictionController {

    private final PredictionCronScheduler predictionCronScheduler;

    @PostMapping("/run-daily")
    public void runDailyPredictions() {
        log.info("[TEST] Manually triggering daily predictions...");
        predictionCronScheduler.runDailyPredictions();
    }

    @PostMapping("/run-monthly")
    public void runMonthlyPredictions() {
        log.info("[TEST] Manually triggering monthly predictions...");
        predictionCronScheduler.runMonthlyPredictions();
    }
}
