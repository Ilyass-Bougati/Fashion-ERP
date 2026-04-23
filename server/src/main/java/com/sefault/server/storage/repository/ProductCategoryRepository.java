package com.sefault.server.storage.repository;

import com.sefault.server.storage.dto.projection.ProductCategoryProjection;
import com.sefault.server.storage.entity.ProductCategory;
import java.util.Optional;
import java.util.UUID;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductCategoryRepository extends JpaRepository<@NonNull ProductCategory, @NonNull UUID> {
    Optional<ProductCategoryProjection> getProductCategoryProjectionById(UUID id);
}
