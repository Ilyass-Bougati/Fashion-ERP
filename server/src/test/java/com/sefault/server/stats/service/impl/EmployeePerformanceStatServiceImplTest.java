package com.sefault.server.stats.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import com.sefault.server.finance.repository.PayrollRepository;
import com.sefault.server.sales.repository.SaleRepository;
import com.sefault.server.stats.dto.projection.EmployeeCommissionProjection;
import com.sefault.server.stats.dto.projection.EmployeeSalesProjection;
import com.sefault.server.stats.entity.EmployeePerformanceStat;
import com.sefault.server.stats.enums.PeriodType;
import com.sefault.server.stats.repository.EmployeePerformanceStatRepository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EmployeePerformanceStatServiceImplTest {

    @Mock
    private SaleRepository saleRepository;

    @Mock
    private PayrollRepository payrollRepository;

    @Mock
    private EmployeePerformanceStatRepository statRepository;

    @InjectMocks
    private EmployeePerformanceStatServiceImpl service;

    @Test
    void saveEmployeeStats_MergesSalesAndCommissionCorrectly() {
        LocalDateTime start = LocalDateTime.now().minusDays(1);
        LocalDateTime end = LocalDateTime.now();
        LocalDate anchor = LocalDate.now();
        String testCin = "CIN123";

        // Mock Projections
        EmployeeSalesProjection salesProj = mock(EmployeeSalesProjection.class);
        when(salesProj.getCin()).thenReturn(testCin);
        when(salesProj.getFirstName()).thenReturn("John");
        when(salesProj.getLastName()).thenReturn("Doe");
        when(salesProj.getSalesCount()).thenReturn(5L);
        when(salesProj.getGrossSalesAmount()).thenReturn(500.0);
        when(salesProj.getItemsSold()).thenReturn(10L);
        when(salesProj.getAvgDiscountGiven()).thenReturn(0.1);

        EmployeeCommissionProjection commProj = mock(EmployeeCommissionProjection.class);
        when(commProj.getCin()).thenReturn(testCin);
        when(commProj.getTotalCommission()).thenReturn(50.0);

        when(saleRepository.aggregateSalesByEmployee(start, end)).thenReturn(List.of(salesProj));
        when(payrollRepository.aggregateCommissionByEmployee(start, end)).thenReturn(List.of(commProj));
        when(statRepository.findByStatDateAndPeriodTypeAndEmployeeCin(anchor, PeriodType.DAILY, testCin))
                .thenReturn(Optional.empty());

        // Execute
        service.saveEmployeeStats(start, end, anchor, PeriodType.DAILY);

        // Verify
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<EmployeePerformanceStat>> captor = ArgumentCaptor.forClass(List.class);
        verify(statRepository).saveAll(captor.capture());

        List<EmployeePerformanceStat> savedList = captor.getValue();
        assertEquals(1, savedList.size());

        EmployeePerformanceStat stat = savedList.get(0);
        assertEquals(testCin, stat.getEmployeeCin());
        assertEquals("John Doe", stat.getEmployeeFullName());
        assertEquals(5, stat.getSalesCount());
        assertEquals(500.0, stat.getGrossSalesAmount());
        assertEquals(50.0, stat.getCommissionEarned());
    }
}
