package com.sefault.server.sales.service;

import com.sefault.server.sales.dto.record.SaleRecord;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface SaleService {
    SaleRecord create(SaleRecord record);

    SaleRecord update(UUID id, SaleRecord record);

    SaleRecord getById(UUID id);

    Page<SaleRecord> getAll(Pageable pageable);

    void delete(UUID id);
}
