package com.sefault.server.finance.repository;

import com.sefault.server.finance.dto.projection.PayrollProjection;
import com.sefault.server.finance.entity.Payroll;
import java.util.Optional;
import java.util.UUID;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PayrollRepository extends JpaRepository<@NonNull Payroll, @NonNull UUID> {
    Optional<PayrollProjection> getPayrollProjectionById(UUID id);
}
