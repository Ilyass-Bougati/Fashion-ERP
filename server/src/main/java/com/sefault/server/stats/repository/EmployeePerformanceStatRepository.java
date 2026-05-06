package com.sefault.server.stats.repository;

import com.sefault.server.stats.dto.projection.EmployeePerformanceStatProjection;
import com.sefault.server.stats.entity.EmployeePerformanceStat;
import java.util.Optional;
import java.util.UUID;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeePerformanceStatRepository
        extends JpaRepository<@NonNull EmployeePerformanceStat, @NonNull UUID> {
    Optional<EmployeePerformanceStatProjection> getEmployeePerformanceStatProjectionById(UUID id);
}
