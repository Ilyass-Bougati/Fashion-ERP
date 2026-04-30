package com.sefault.server.finance.service;

import com.sefault.server.finance.dto.record.FixChargeRecord;
import com.sefault.server.finance.dto.record.PayrollRecord;
import com.sefault.server.finance.dto.record.TransactionRecord;
import com.sefault.server.finance.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.UUID;

public interface FinanceService {
    // --- TRANSACTIONS ---
    TransactionRecord createTransaction(TransactionRecord transactionRecord);

    TransactionRecord getTransaction(UUID transactionId);

    TransactionRecord reverseTransaction(UUID originalTransactionId);

    Page<TransactionRecord> getAllTransactions(Pageable pageable);

    Page<TransactionRecord> getTransactionsByType(TransactionType type, Pageable pageable);

    // --- PAYROLL ---
    PayrollRecord processPayroll(UUID employeeId, LocalDateTime startDate, LocalDateTime endDate);

    PayrollRecord getPayroll(UUID payrollId);

    Page<PayrollRecord> getAllPayrolls(Pageable pageable);

    Page<PayrollRecord> getPayrollHistoryForEmployee(UUID employeeId, Pageable pageable);

    // --- FIXED CHARGES ---
    FixChargeRecord createFixCharge(FixChargeRecord fixChargeRecord);

    FixChargeRecord getFixCharge(UUID chargeId);

    FixChargeRecord updateFixCharge(FixChargeRecord fixChargeRecord);

    void toggleFixChargeStatus(UUID chargeId);

    Page<FixChargeRecord> getAllFixCharges(Pageable pageable);

    Page<FixChargeRecord> getActiveFixCharges(Pageable pageable);
}
