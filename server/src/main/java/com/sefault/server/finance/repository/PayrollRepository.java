package com.sefault.server.finance.repository;

import com.sefault.server.finance.dto.projection.PayrollProjection;
import com.sefault.server.finance.entity.Payroll;
import com.sefault.server.stats.dto.projection.EmployeeCommissionProjection;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PayrollRepository extends JpaRepository<@NonNull Payroll, @NonNull UUID> {
    Optional<PayrollProjection> getPayrollProjectionById(UUID id);

    Page<PayrollProjection> findAllBy(Pageable pageable);

    Page<PayrollProjection> findByEmployeeId(UUID employeeId, Pageable pageable);

    @Query("""
        SELECT COALESCE(SUM(p.salary + p.commission), 0.0)
        FROM Payroll p
        WHERE p.createdAt BETWEEN :startDate AND :endDate
    """)
    Double calculateTotalPayrollForPeriod(
            @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("""
        SELECT e.CIN AS cin,
               e.firstName AS firstName,
               e.lastName AS lastName,
               COALESCE(SUM(p.commission), 0.0) AS totalCommission
        FROM Payroll p
        JOIN p.employee e
        WHERE p.createdAt >= :start AND p.createdAt < :end
        GROUP BY e.id, e.CIN, e.firstName, e.lastName
    """)
    List<EmployeeCommissionProjection> aggregateCommissionByEmployee(
            @Param("start") LocalDateTime start, @Param("end") LocalDateTime end);
}
