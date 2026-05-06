package com.sefault.server.stats.repository;

import com.sefault.server.stats.dto.projection.FinancialStatProjection;
import com.sefault.server.stats.entity.FinancialStat;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FinancialStatRepository extends JpaRepository<@NonNull FinancialStat, @NonNull UUID> {
    Optional<FinancialStatProjection> findByFinancialStatId(UUID id);
}
