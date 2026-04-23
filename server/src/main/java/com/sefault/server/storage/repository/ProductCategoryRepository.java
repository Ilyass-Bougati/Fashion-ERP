package com.sefault.server.storage.repository;

import com.sefault.server.storage.dto.projection.ProductCategoryProjection;
import com.sefault.server.storage.entity.ProductCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import lombok.NonNull;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductCategoryRepository extends JpaRepository<@NonNull ProductCategory, @NonNull UUID> {
    Optional<ProductCategoryProjection> getProductCategoryProjectionById(UUID id);
}