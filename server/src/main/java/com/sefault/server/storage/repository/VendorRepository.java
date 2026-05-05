package com.sefault.server.storage.repository;

import com.sefault.server.storage.dto.projection.VendorProjection;
import com.sefault.server.storage.entity.Vendor;
import java.util.Optional;
import java.util.UUID;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VendorRepository extends JpaRepository<@NonNull Vendor, @NonNull UUID> {
    Page<VendorProjection> findAllBy(Pageable pageable);

    Optional<VendorProjection> getVendorProjectionById(UUID id);
}
