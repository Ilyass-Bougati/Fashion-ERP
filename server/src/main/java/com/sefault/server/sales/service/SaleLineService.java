package com.sefault.server.sales.service;

import com.sefault.server.sales.dto.record.SaleLineRecord;
import com.sefault.server.sales.entity.id.SaleLineId;
import java.util.List;
import java.util.UUID;

public interface SaleLineService {
    SaleLineRecord create(SaleLineRecord record);

    SaleLineRecord update(SaleLineId id, SaleLineRecord record);

    SaleLineRecord getById(SaleLineId id);

    List<SaleLineRecord> getBySaleId(UUID saleId);

    void delete(SaleLineId id);
}
