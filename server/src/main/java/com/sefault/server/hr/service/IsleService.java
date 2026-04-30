package com.sefault.server.hr.service;

import com.sefault.server.hr.dto.record.IsleRecord;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IsleService {
    IsleRecord create(IsleRecord record);

    IsleRecord update(UUID id, IsleRecord record);

    IsleRecord getById(UUID id);

    Page<IsleRecord> getAll(Pageable pageable);

    List<IsleRecord> getByEmployee(UUID employeeId);

    void delete(UUID id);
}
