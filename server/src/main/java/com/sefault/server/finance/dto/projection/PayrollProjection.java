package com.sefault.server.finance.dto.projection;

import com.sefault.server.finance.entity.Transaction;
import com.sefault.server.hr.entity.Employee;

import java.time.LocalDateTime;
import java.util.UUID;

public interface PayrollProjection {
    UUID getId();

    Double getSalary();

    Transaction getTransaction();

    Employee getEmployee();

    Double getCommission();

    LocalDateTime getCreatedAt();
}