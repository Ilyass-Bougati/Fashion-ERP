package com.sefault.server.finance.service;

import com.sefault.server.finance.dto.record.PayrollRecord;
import java.time.LocalDateTime;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PayrollService {
    PayrollRecord processPayroll(UUID employeeId, LocalDateTime startDate, LocalDateTime endDate);

    PayrollRecord getPayroll(UUID payrollId);

    Page<PayrollRecord> getAllPayrolls(Pageable pageable);

    Page<PayrollRecord> getPayrollHistoryForEmployee(UUID employeeId, Pageable pageable);
}
