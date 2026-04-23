package com.sefault.server.finance.repository;

import com.sefault.server.finance.dto.projection.PayrollProjection;
import com.sefault.server.finance.entity.Payroll;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import lombok.NonNull;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PayrollRepository extends JpaRepository<@NonNull Payroll, @NonNull UUID> {
    Optional<PayrollProjection> getPayrollProjectionById(UUID id);
}