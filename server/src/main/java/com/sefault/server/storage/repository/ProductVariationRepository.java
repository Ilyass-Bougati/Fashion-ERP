package com.sefault.server.storage.repository;

import com.sefault.server.storage.dto.projection.ProductVariationProjection;
import com.sefault.server.storage.entity.ProductVariation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import lombok.NonNull;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductVariationRepository extends JpaRepository<@NonNull ProductVariation, @NonNull UUID> {
    Optional<ProductVariationProjection> getProductVariationProjectionById(UUID id);
}