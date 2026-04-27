package com.sefault.server.storage.repository;

import com.sefault.server.storage.dto.projection.ProductProjection;
import com.sefault.server.storage.entity.Product;
import java.util.Optional;
import java.util.UUID;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<@NonNull Product, @NonNull UUID> {
    Optional<ProductProjection> getProductProjectionById(UUID id);
}
