package com.sefault.server.finance.dto.record;

import com.sefault.server.finance.entity.Transaction;
import com.sefault.server.hr.entity.Employee;
import java.time.LocalDateTime;
import java.util.UUID;

public record PayrollRecord(
        UUID id,
        Double salary,
        Transaction transaction,
        Employee employee,
        Double commission,
        LocalDateTime createdAt) {}
