package com.sefault.server.stats.service;

import com.sefault.server.stats.enums.PeriodType;

import java.time.LocalDate;

public interface StockStatService {
    void saveStockStats(LocalDate anchorDate, PeriodType periodType);
}
