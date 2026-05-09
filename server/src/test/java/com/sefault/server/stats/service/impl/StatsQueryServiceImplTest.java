package com.sefault.server.stats.service.impl;

import static org.mockito.Mockito.verify;

import com.sefault.server.stats.enums.PeriodType;
import com.sefault.server.stats.repository.EmployeePerformanceStatRepository;
import com.sefault.server.stats.repository.FinancialStatRepository;
import com.sefault.server.stats.repository.SalesStatRepository;
import com.sefault.server.stats.repository.StockStatRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class StatsQueryServiceImplTest {

    @Mock
    private FinancialStatRepository financialRepo;

    @Mock
    private SalesStatRepository salesRepo;

    @Mock
    private StockStatRepository stockRepo;

    @Mock
    private EmployeePerformanceStatRepository employeeRepo;

    @InjectMocks
    private StatsQueryServiceImpl service;

    @Test
    void getFinancialStats_CallsRepository() {
        Pageable pageable = PageRequest.of(0, 10);
        service.getFinancialStats(PeriodType.MONTHLY, pageable);
        verify(financialRepo).findByPeriodType(PeriodType.MONTHLY, pageable);
    }

    @Test
    void getSalesStats_CallsRepository() {
        Pageable pageable = PageRequest.of(0, 10);
        service.getSalesStats(PeriodType.DAILY, pageable);
        verify(salesRepo).findByPeriodType(PeriodType.DAILY, pageable);
    }

    @Test
    void getEmployeePerformanceStats_CallsRepository() {
        Pageable pageable = PageRequest.of(0, 10);
        service.getEmployeePerformanceStats(PeriodType.WEEKLY, pageable);
        verify(employeeRepo).findByPeriodType(PeriodType.WEEKLY, pageable);
    }

    @Test
    void getStockStats_CallsRepository() {
        Pageable pageable = PageRequest.of(0, 10);
        LocalDate date = LocalDate.now();
        service.getStockStats(date, PeriodType.DAILY, pageable);
        verify(stockRepo).findByStatDateAndPeriodType(date, PeriodType.DAILY, pageable);
    }
}
