package com.sefault.server.storage.repository;

import com.sefault.server.storage.dto.projection.ProductVariationProjection;
import com.sefault.server.storage.entity.ProductVariation;
import java.util.Optional;
import java.util.UUID;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductVariationRepository extends JpaRepository<@NonNull ProductVariation, @NonNull UUID> {
    Optional<ProductVariationProjection> getProductVariationProjectionById(UUID id);

    @Modifying
    @Query("UPDATE ProductVariation p SET p.quantity = p.quantity + :amount WHERE p.id = :id")
    void incrementStock(@Param("id") UUID id, @Param("amount") int amount);
}
