package com.sefault.server.hr.service;

import com.sefault.server.hr.dto.record.EmployeeRecord;

import java.util.List;
import java.util.UUID;

public interface EmployeeService {
    EmployeeRecord create(EmployeeRecord record);
    EmployeeRecord update(UUID id, EmployeeRecord record);
    EmployeeRecord getById(UUID id);
    List<EmployeeRecord> getAll();
    void delete(UUID id);

    List<EmployeeRecord> getActive();
    EmployeeRecord terminate(UUID id);
}
