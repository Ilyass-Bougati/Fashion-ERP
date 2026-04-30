package com.sefault.server.finance.controller;

import com.sefault.server.finance.dto.record.FixChargeRecord;
import com.sefault.server.finance.dto.record.PayrollRecord;
import com.sefault.server.finance.dto.record.TransactionRecord;
import com.sefault.server.finance.enums.TransactionType;
import com.sefault.server.finance.service.FinanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/finance")
@RequiredArgsConstructor
public class FinanceController {
    private final FinanceService financeService;

    @PostMapping("/transactions")
    public ResponseEntity<TransactionRecord> createTransactionRecord(@RequestBody @Valid TransactionRecord transactionRecord){
        return ResponseEntity.ok(financeService.createTransaction(transactionRecord));
    }

    @GetMapping("/transactions/{id}")
    public ResponseEntity<TransactionRecord> getTransaction(@PathVariable UUID id){
        return ResponseEntity.ok(financeService.getTransaction(id));
    }

    @PostMapping("/transactions/{id}/reverse")
    public ResponseEntity<TransactionRecord> reverseTransaction(@PathVariable UUID id){
        return ResponseEntity.ok(financeService.reverseTransaction(id));
    }

    @GetMapping("/transactions")
    public ResponseEntity<Page<TransactionRecord>> getAllTransactions(
            @RequestParam(required = false) TransactionType type,
            @PageableDefault(size = 20) Pageable pageable){
            if (type != null) {
                return ResponseEntity.ok(financeService.getTransactionsByType(type, pageable));
            }
            return ResponseEntity.ok(financeService.getAllTransactions(pageable));
    }

    @PostMapping("/payroll/process/{employeeId}")
    public ResponseEntity<PayrollRecord> payrollProcess(
            @PathVariable UUID employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate){
        return ResponseEntity.ok(financeService.processPayroll(employeeId, startDate, endDate));
    }

    @GetMapping("/payroll/{id}")
    public ResponseEntity<PayrollRecord> getPayroll(@PathVariable UUID id) {
        return ResponseEntity.ok(financeService.getPayroll(id));
    }

    @GetMapping("/payroll")
    public ResponseEntity<Page<PayrollRecord>> getAllPayrolls(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(financeService.getAllPayrolls(pageable));
    }

    @GetMapping("/payroll/employee/{employeeId}")
    public ResponseEntity<Page<PayrollRecord>> getPayrollHistoryForEmployee(
            @PathVariable UUID employeeId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(financeService.getPayrollHistoryForEmployee(employeeId, pageable));
    }

    @PostMapping("/fixed-charges")
    public ResponseEntity<FixChargeRecord> createFixCharge(@RequestBody FixChargeRecord record) {
        return ResponseEntity.ok(financeService.createFixCharge(record));
    }

    @GetMapping("/fixed-charges/{id}")
    public ResponseEntity<FixChargeRecord> getFixCharge(@PathVariable UUID id) {
        return ResponseEntity.ok(financeService.getFixCharge(id));
    }

    @PutMapping("/fixed-charges")
    public ResponseEntity<FixChargeRecord> updateFixCharge(@RequestBody FixChargeRecord record) {
        return ResponseEntity.ok(financeService.updateFixCharge(record));
    }

    @PatchMapping("/fixed-charges/{id}/toggle")
    public ResponseEntity<Void> toggleFixChargeStatus(@PathVariable UUID id) {
        financeService.toggleFixChargeStatus(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/fixed-charges")
    public ResponseEntity<Page<FixChargeRecord>> getAllFixCharges(
            @RequestParam(required = false, defaultValue = "false") boolean activeOnly,
            @PageableDefault(size = 20) Pageable pageable) {
        if (activeOnly) {
            return ResponseEntity.ok(financeService.getActiveFixCharges(pageable));
        }
        return ResponseEntity.ok(financeService.getAllFixCharges(pageable));
    }
}
