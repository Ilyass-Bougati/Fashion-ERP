package com.sefault.server.hr.service;

import com.sefault.server.hr.dto.record.IsleRecord;
import java.util.List;
import java.util.UUID;

public interface IsleService {
    IsleRecord create(IsleRecord record);

    IsleRecord update(UUID id, IsleRecord record);

    IsleRecord getById(UUID id);

    List<IsleRecord> getAll();

    List<IsleRecord> getByEmployee(UUID employeeId);

    void delete(UUID id);

    IsleRecord assignEmployee(UUID isleId, UUID employeeId);

    IsleRecord unassignEmployee(UUID isleId);
}
