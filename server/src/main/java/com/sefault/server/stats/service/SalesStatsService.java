package com.sefault.server.stats.service;

import com.sefault.server.stats.enums.PeriodType;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface SalesStatsService {
    void saveSalesStats(LocalDateTime start, LocalDateTime end, LocalDate anchorDate, PeriodType periodType);
}
