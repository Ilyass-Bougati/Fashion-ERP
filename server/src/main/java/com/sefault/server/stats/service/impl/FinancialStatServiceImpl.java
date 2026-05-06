package com.sefault.server.stats.service.impl;

import com.sefault.server.finance.repository.FixChargeRepository;
import com.sefault.server.finance.repository.PayrollRepository;
import com.sefault.server.hr.repository.EmployeeRepository;
import com.sefault.server.sales.repository.SaleRepository;
import com.sefault.server.stats.entity.FinancialStat;
import com.sefault.server.stats.enums.PeriodType;
import com.sefault.server.stats.repository.FinancialStatRepository;
import com.sefault.server.stats.service.FinancialStatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
public class FinancialStatServiceImpl implements FinancialStatService {
    private final SaleRepository saleRepository;
    private final PayrollRepository payrollRepository;
    private final FixChargeRepository fixChargeRepository;
    private final EmployeeRepository employeeRepository;
    private final FinancialStatRepository financialStatRepository;

    public void saveFinancialStats(LocalDateTime start, LocalDateTime end, LocalDate anchorDate, PeriodType periodType) {
        if (periodType == PeriodType.DAILY || periodType == PeriodType.WEEKLY) {
            throw new IllegalArgumentException("Financial stats are only calculated for MONTHLY or YEARLY periods.");
        }

        Double totalRevenue = saleRepository.calculateTotalNetRevenueForPeriod(start, end);
        Double totalPayrollCost = payrollRepository.calculateTotalPayrollForPeriod(start, end);
        Double totalFixCharges = fixChargeRepository.calculateActiveFixCharges();
        Integer activeEmployeesCount = employeeRepository.countByActiveTrue();

        Double grossProfit = totalRevenue - totalPayrollCost;
        Double netProfit = grossProfit - totalFixCharges;

        Double profitMarginPct = totalRevenue > 0 ? (netProfit / totalRevenue) * 100 : 0.0;
        Double revenuePerEmployee = activeEmployeesCount > 0 ? (totalRevenue / activeEmployeesCount) : 0.0;

        FinancialStat existingStat = financialStatRepository
                .findByStatDateAndPeriodType(anchorDate, periodType)
                .orElse(null);

        FinancialStat financialStat = FinancialStat.builder()
                .id(existingStat != null ? existingStat.getId() : null)
                .computedAt(existingStat != null ? existingStat.getComputedAt() : null)
                .statDate(anchorDate)
                .periodType(periodType)
                .totalRevenue(totalRevenue)
                .totalPayrollCost(totalPayrollCost)
                .totalFixCharges(totalFixCharges)
                .grossProfit(grossProfit)
                .netProfit(netProfit)
                .profitMarginPct(profitMarginPct)
                .activeEmployeesCount(activeEmployeesCount)
                .revenuePerEmployee(revenuePerEmployee)
                .reconciledAt(LocalDateTime.now())
                .build();

        financialStatRepository.save(financialStat);
    }

}
