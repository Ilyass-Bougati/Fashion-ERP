package com.sefault.server.storage.repository;

import com.sefault.server.storage.dto.projection.VendorProjection;
import com.sefault.server.storage.entity.Vendor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import lombok.NonNull;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface VendorRepository extends JpaRepository<@NonNull Vendor, @NonNull UUID> {
    Optional<VendorProjection> getVendorProjectionById(UUID id);
}