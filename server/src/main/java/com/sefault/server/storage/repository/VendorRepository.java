package com.sefault.server.storage.repository;

import com.sefault.server.storage.dto.projection.VendorProjection;
import com.sefault.server.storage.entity.Vendor;
import java.util.Optional;
import java.util.UUID;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VendorRepository extends JpaRepository<@NonNull Vendor, @NonNull UUID> {
    Optional<VendorProjection> getVendorProjectionById(UUID id);
}
