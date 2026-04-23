package com.sefault.server.sales.repository;

import com.sefault.server.sales.dto.projection.SaleLineProjection;
import com.sefault.server.sales.entity.SaleLine;
import java.util.Optional;
import java.util.UUID;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SaleLineRepository extends JpaRepository<@NonNull SaleLine, @NonNull UUID> {
    Optional<SaleLineProjection> getSaleLineProjectionById(UUID id);
}
