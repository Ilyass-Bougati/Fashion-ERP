package com.sefault.server.storage.repository;

import com.sefault.server.storage.dto.projection.ProductProjection;
import com.sefault.server.storage.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import lombok.NonNull;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<@NonNull Product, @NonNull UUID> {
    Optional<ProductProjection> getProductProjectionById(UUID id);
}