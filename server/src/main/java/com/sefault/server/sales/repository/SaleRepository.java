package com.sefault.server.sales.repository;

import com.sefault.server.sales.dto.projection.SaleProjection;
import com.sefault.server.sales.entity.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import lombok.NonNull;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SaleRepository extends JpaRepository<@NonNull Sale, @NonNull UUID> {
    Optional<SaleProjection> getSaleProjectionById(UUID id);
}