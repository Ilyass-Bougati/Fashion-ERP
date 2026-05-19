package com.sefault.server.stats.service.impl;

import com.sefault.server.finance.repository.PayrollRepository;
import com.sefault.server.sales.repository.SaleRepository;
import com.sefault.server.stats.dto.projection.EmployeeCommissionProjection;
import com.sefault.server.stats.dto.projection.EmployeeSalesProjection;
import com.sefault.server.stats.entity.EmployeePerformanceStat;
import com.sefault.server.stats.enums.PeriodType;
import com.sefault.server.stats.repository.EmployeePerformanceStatRepository;
import com.sefault.server.stats.service.EmployeePerformanceStatService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeePerformanceStatServiceImpl implements EmployeePerformanceStatService {
    private final SaleRepository saleRepository;
    private final PayrollRepository payrollRepository;
    private final EmployeePerformanceStatRepository statRepository;

    public void saveEmployeeStats(LocalDateTime start, LocalDateTime end, LocalDate anchorDate, PeriodType periodType) {

        List<EmployeeSalesProjection> salesAgg = saleRepository.aggregateSalesByEmployee(start, end);

        Map<String, EmployeeSalesProjection> salesMap =
                salesAgg.stream().collect(Collectors.toMap(EmployeeSalesProjection::getCin, proj -> proj));

        Map<String, Double> commissionMap = payrollRepository.aggregateCommissionByEmployee(start, end).stream()
                .collect(Collectors.toMap(
                        EmployeeCommissionProjection::getCin, EmployeeCommissionProjection::getTotalCommission));

        List<EmployeePerformanceStat> statsToSave = new ArrayList<>();

        for (String cin : salesMap.keySet()) {
            EmployeeSalesProjection sData = salesMap.get(cin);

            String fullName = sData.getFirstName() + " " + sData.getLastName();

            Double commissionEarned = commissionMap.getOrDefault(cin, 0.0);

            EmployeePerformanceStat existingStat = statRepository
                    .findByStatDateAndPeriodTypeAndEmployeeCin(anchorDate, periodType, cin)
                    .orElse(null);

            EmployeePerformanceStat stat = EmployeePerformanceStat.builder()
                    .id(existingStat != null ? existingStat.getId() : null)
                    .computedAt(existingStat != null ? existingStat.getComputedAt() : null)
                    .statDate(anchorDate)
                    .periodType(periodType)
                    .employeeCin(cin)
                    .employeeFullName(fullName)
                    .salesCount(sData.getSalesCount().intValue())
                    .grossSalesAmount(sData.getGrossSalesAmount())
                    .itemsSold(sData.getItemsSold().intValue())
                    .avgDiscountGiven(sData.getAvgDiscountGiven())
                    .commissionEarned(commissionEarned)
                    .reconciledAt(LocalDateTime.now())
                    .build();

            statsToSave.add(stat);
        }

        statRepository.saveAll(statsToSave);
    }
}
