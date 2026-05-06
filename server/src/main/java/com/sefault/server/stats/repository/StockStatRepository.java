package com.sefault.server.stats.repository;

import com.sefault.server.stats.dto.projection.StockStatProjection;
import com.sefault.server.stats.entity.StockStat;
import java.util.Optional;
import java.util.UUID;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface StockStatRepository extends JpaRepository<@NonNull StockStat, @NonNull UUID> {
    Optional<StockStatProjection> getStockStatProjectionById(UUID id);
}
