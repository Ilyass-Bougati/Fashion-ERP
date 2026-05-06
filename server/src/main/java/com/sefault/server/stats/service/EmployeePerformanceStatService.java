package com.sefault.server.stats.service;

import com.sefault.server.stats.enums.PeriodType;

import java.time.LocalDate;
import java.time.LocalDateTime;

public interface EmployeePerformanceStatService {
    void saveEmployeeStats(LocalDateTime start, LocalDateTime end, LocalDate anchorDate, PeriodType periodType);
}
