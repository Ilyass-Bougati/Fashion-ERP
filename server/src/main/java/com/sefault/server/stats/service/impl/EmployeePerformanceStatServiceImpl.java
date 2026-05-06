package com.sefault.server.stats.service.impl;

import com.sefault.server.finance.repository.PayrollRepository;
import com.sefault.server.sales.repository.SaleRepository;
import com.sefault.server.stats.dto.projection.EmployeeCommissionProjection;
import com.sefault.server.stats.dto.projection.EmployeeSalesProjection;
import com.sefault.server.stats.entity.EmployeePerformanceStat;
import com.sefault.server.stats.enums.PeriodType;
import com.sefault.server.stats.repository.EmployeePerformanceStatRepository;
import com.sefault.server.stats.service.EmployeePerformanceStatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeePerformanceStatServiceImpl implements EmployeePerformanceStatService {
    private final SaleRepository saleRepository;
    private final PayrollRepository payrollRepository;
    private final EmployeePerformanceStatRepository statRepository;

    public void saveEmployeeStats(LocalDateTime start, LocalDateTime end, LocalDate anchorDate, PeriodType periodType) {

        List<EmployeeSalesProjection> salesAgg = saleRepository.aggregateSalesByEmployee(start, end);
        List<EmployeeCommissionProjection> commAgg = payrollRepository.aggregateCommissionByEmployee(start, end);

        Map<String, EmployeeSalesProjection> salesMap = salesAgg.stream()
                .collect(Collectors.toMap(EmployeeSalesProjection::getCin, proj -> proj));

        Map<String, EmployeeCommissionProjection> commissionMap = commAgg.stream()
                .collect(Collectors.toMap(EmployeeCommissionProjection::getCin, proj -> proj));

        Set<String> activeCins = new HashSet<>();
        activeCins.addAll(salesMap.keySet());
        activeCins.addAll(commissionMap.keySet());

        List<EmployeePerformanceStat> statsToSave = new ArrayList<>();

        for (String cin : activeCins) {
            EmployeeSalesProjection sData = salesMap.get(cin);
            EmployeeCommissionProjection cData = commissionMap.get(cin);

            String fullName = sData != null
                    ? sData.getFirstName() + " " + sData.getLastName()
                    : cData.getFirstName() + " " + cData.getLastName();

            Double commissionEarned = cData != null ? cData.getTotalCommission() : 0.0;

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
                    .salesCount(sData != null ? sData.getSalesCount().intValue() : 0)
                    .grossSalesAmount(sData != null ? sData.getGrossSalesAmount() : 0.0)
                    .itemsSold(sData != null ? sData.getItemsSold().intValue() : 0)
                    .avgDiscountGiven(sData != null ? sData.getAvgDiscountGiven() : 0.0)
                    .commissionEarned(commissionEarned)
                     .reconciledAt(LocalDateTime.now())
                    .build();

            statsToSave.add(stat);
        }

        statRepository.saveAll(statsToSave);
    }
}
