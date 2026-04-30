package com.sefault.server.storage.service;

import com.sefault.server.storage.dto.projection.VendorProjection;
import com.sefault.server.storage.dto.record.VendorRecord;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface VendorService {
    Page<VendorProjection> findAllPaginated(Pageable pageable);

    VendorProjection getById(UUID id);

    VendorRecord save(VendorRecord vendor);

    VendorRecord update(VendorRecord vendor);

    void delete(UUID id);
}
