package com.sefault.server.finance.controller;

import com.sefault.server.finance.dto.record.FixChargeRecord;
import com.sefault.server.finance.dto.record.PayrollRecord;
import com.sefault.server.finance.dto.record.TransactionRecord;
import com.sefault.server.finance.enums.TransactionType;
import com.sefault.server.finance.service.FinanceService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/finance")
@RequiredArgsConstructor
@Tag(name = "Finance & Accounting", description = "Endpoints for managing immutable transactions, employee payroll, and recurring fixed charges.")
public class FinanceController {
    private final FinanceService financeService;

    @Operation(summary = "Create a manual transaction", description = "Creates a generic inflow or outflow transaction.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transaction created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid payload"),
            @ApiResponse(responseCode = "403", description = "Insufficient authorities")
    })
    @PostMapping("/transactions")
    @PreAuthorize("hasAuthority(@authorities.createTransactionAuthority)")
    public ResponseEntity<TransactionRecord> createTransactionRecord(@RequestBody @Valid TransactionRecord transactionRecord){
        return ResponseEntity.ok(financeService.createTransaction(transactionRecord));
    }

    @Operation(summary = "Get transaction details", description = "Fetch a specific transaction by its UUID.")
    @ApiResponse(responseCode = "404", description = "Transaction not found")
    @GetMapping("/transactions/{id}")
    @PreAuthorize("hasAuthority(@authorities.readTransactionAuthority)")
    public ResponseEntity<TransactionRecord> getTransaction(@PathVariable UUID id){
        return ResponseEntity.ok(financeService.getTransaction(id));
    }

    @Operation(summary = "Reverse a transaction", description = "Creates a counter-balancing transaction (RECEIVED for PAID, and vice versa) to correct ledger mistakes or refunding.")
    @PostMapping("/transactions/{id}/reverse")
    @PreAuthorize("hasAuthority(@authorities.reverseTransactionAuthority)")
    public ResponseEntity<TransactionRecord> reverseTransaction(@PathVariable UUID id){
        return ResponseEntity.ok(financeService.reverseTransaction(id));
    }

    @Operation(summary = "Get all transactions", description = "Fetch a paginated ledger of transactions. Can optionally be filtered by transaction type.")
    @GetMapping("/transactions")
    @PreAuthorize("hasAuthority(@authorities.readTransactionAuthority)")
    public ResponseEntity<Page<TransactionRecord>> getAllTransactions(
            @RequestParam(required = false) TransactionType type,
            @PageableDefault(size = 20) Pageable pageable){
            if (type != null) {
                return ResponseEntity.ok(financeService.getTransactionsByType(type, pageable));
            }
            return ResponseEntity.ok(financeService.getAllTransactions(pageable));
    }

    @Operation(summary = "Process employee payroll", description = "Calculates commissions based on sales within the specified date range and creates a secured payroll transaction.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payroll processed successfully"),
            @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    @PostMapping("/payroll/process/{employeeId}")
    @PreAuthorize("hasAuthority(@authorities.processPayrollAuthority)")
    public ResponseEntity<PayrollRecord> payrollProcess(
            @PathVariable UUID employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate){
        return ResponseEntity.ok(financeService.processPayroll(employeeId, startDate, endDate));
    }

    @Operation(summary = "Get payroll details", description = "Fetch a specific payroll run by its UUID.")
    @GetMapping("/payroll/{id}")
    @PreAuthorize("hasAnyAuthority(@authorities.readPayrollAuthority)")
    public ResponseEntity<PayrollRecord> getPayroll(@PathVariable UUID id) {
        return ResponseEntity.ok(financeService.getPayroll(id));
    }

    @Operation(summary = "Get all payroll history", description = "Fetch a paginated list of all company payroll records.")
    @GetMapping("/payroll")
    @PreAuthorize("hasAnyAuthority(@authorities.readPayrollAuthority)")
    public ResponseEntity<Page<PayrollRecord>> getAllPayrolls(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(financeService.getAllPayrolls(pageable));
    }

    @Operation(summary = "Get payroll by employee", description = "Fetch the paginated payroll history for a specific employee.")
    @GetMapping("/payroll/employee/{employeeId}")
    @PreAuthorize("hasAnyAuthority(@authorities.readPayrollAuthority)")
    public ResponseEntity<Page<PayrollRecord>> getPayrollHistoryForEmployee(
            @PathVariable UUID employeeId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(financeService.getPayrollHistoryForEmployee(employeeId, pageable));
    }

    @Operation(summary = "Create a fixed charge", description = "Register a new recurring charge (e.g., Rent, Software).")
    @PostMapping("/fixed-charges")
    @PreAuthorize("hasAuthority(@authorities.createFixedChargeAuthority)")
    public ResponseEntity<FixChargeRecord> createFixCharge(@RequestBody FixChargeRecord record) {
        return ResponseEntity.ok(financeService.createFixCharge(record));
    }

    @Operation(summary = "Get fixed charge details", description = "Fetch a specific fixed charge by its UUID.")
    @GetMapping("/fixed-charges/{id}")
    @PreAuthorize("hasAuthority(@authorities.readFixedChargeAuthority)")
    public ResponseEntity<FixChargeRecord> getFixCharge(@PathVariable UUID id) {
        return ResponseEntity.ok(financeService.getFixCharge(id));
    }

    @Operation(summary = "Update a fixed charge", description = "Update the details or amount of an existing fixed charge.")
    @PutMapping("/fixed-charges")
    @PreAuthorize("hasAuthority(@authorities.updateFixedChargeAuthority)")
    public ResponseEntity<FixChargeRecord> updateFixCharge(@RequestBody FixChargeRecord record) {
        return ResponseEntity.ok(financeService.updateFixCharge(record));
    }

    @Operation(summary = "Toggle charge status", description = "Activate or deactivate a fixed charge.")
    @ApiResponse(responseCode = "204", description = "Status toggled successfully")
    @PatchMapping("/fixed-charges/{id}/toggle")
    @PreAuthorize("hasAuthority(@authorities.toggleFixedChargeAuthority)")
    public ResponseEntity<Void> toggleFixChargeStatus(@PathVariable UUID id) {
        financeService.toggleFixChargeStatus(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get all fixed charges", description = "Fetch a paginated list of fixed charges, with an optional filter for active ones only.")
    @GetMapping("/fixed-charges")
    @PreAuthorize("hasAuthority(@authorities.readFixedChargeAuthority)")
    public ResponseEntity<Page<FixChargeRecord>> getAllFixCharges(
            @RequestParam(required = false, defaultValue = "false") boolean activeOnly,
            @PageableDefault(size = 20) Pageable pageable) {
        if (activeOnly) {
            return ResponseEntity.ok(financeService.getActiveFixCharges(pageable));
        }
        return ResponseEntity.ok(financeService.getAllFixCharges(pageable));
    }
}
