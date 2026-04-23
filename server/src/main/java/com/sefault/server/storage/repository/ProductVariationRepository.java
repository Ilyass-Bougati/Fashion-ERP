package com.sefault.server.storage.repository;

import com.sefault.server.storage.dto.projection.ProductVariationProjection;
import com.sefault.server.storage.entity.ProductVariation;
import java.util.Optional;
import java.util.UUID;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductVariationRepository extends JpaRepository<@NonNull ProductVariation, @NonNull UUID> {
    Optional<ProductVariationProjection> getProductVariationProjectionById(UUID id);
}
