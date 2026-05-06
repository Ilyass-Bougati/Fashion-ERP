package com.sefault.server.stats.repository;

import com.sefault.server.stats.dto.projection.FinancialStatProjection;
import com.sefault.server.stats.entity.FinancialStat;
import java.util.Optional;
import java.util.UUID;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface FinancialStatRepository extends JpaRepository<@NonNull FinancialStat, @NonNull UUID> {
    Optional<FinancialStatProjection> getFinancialStatProjectionById(UUID id);
}
