package com.sefault.server.storage.service;

import com.sefault.server.storage.dto.record.VendorRecord;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface VendorService {
    Page<VendorRecord> findAllPaginated(Pageable pageable);

    VendorRecord getById(UUID id);

    VendorRecord save(VendorRecord record);

    VendorRecord update(UUID id, VendorRecord record);

    void delete(UUID id);
}
