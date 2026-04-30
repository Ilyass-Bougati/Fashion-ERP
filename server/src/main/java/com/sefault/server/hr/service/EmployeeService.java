package com.sefault.server.hr.service;

import com.sefault.server.hr.dto.record.EmployeeRecord;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface EmployeeService {
    EmployeeRecord create(EmployeeRecord record);

    EmployeeRecord update(UUID id, EmployeeRecord record);

    EmployeeRecord getById(UUID id);

    Page<EmployeeRecord> getAll(Pageable pageable);

    void delete(UUID id);

    Page<EmployeeRecord> getActive(Pageable pageable);

    EmployeeRecord terminate(UUID id);
}
