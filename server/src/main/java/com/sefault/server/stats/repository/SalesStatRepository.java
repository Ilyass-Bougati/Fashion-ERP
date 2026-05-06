package com.sefault.server.stats.repository;

import com.sefault.server.stats.dto.projection.SalesStatProjection;
import com.sefault.server.stats.entity.SalesStat;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SalesStatRepository extends JpaRepository<@NonNull SalesStat, @NonNull UUID> {
    Optional<SalesStatProjection> getSalesStatProjectionById(UUID id);
}
