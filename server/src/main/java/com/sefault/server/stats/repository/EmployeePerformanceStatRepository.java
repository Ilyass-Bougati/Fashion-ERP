package com.sefault.server.stats.repository;

import com.sefault.server.stats.dto.projection.EmployeePerformanceStatProjection;
import com.sefault.server.stats.entity.EmployeePerformanceStat;
import com.sefault.server.stats.enums.PeriodType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeePerformanceStatRepository
        extends JpaRepository<@NonNull EmployeePerformanceStat, @NonNull UUID> {
    Optional<EmployeePerformanceStatProjection> getEmployeePerformanceStatProjectionById(UUID id);

    Optional<EmployeePerformanceStat> findByStatDateAndPeriodTypeAndEmployeeCin(
            LocalDate statDate, PeriodType periodType, String employeeCin);

    Page<EmployeePerformanceStatProjection> findByPeriodType(PeriodType periodType, Pageable pageable);

    @Query("SELECT DISTINCT e.employeeCin FROM EmployeePerformanceStat e")
    List<String> findDistinctEmployeeCins();

    List<EmployeePerformanceStat> findTop30ByEmployeeCinAndPeriodTypeOrderByStatDateDesc(
            String cin, PeriodType periodType);
}
