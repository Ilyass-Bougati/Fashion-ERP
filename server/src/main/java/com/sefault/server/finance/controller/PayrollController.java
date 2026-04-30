package com.sefault.server.finance.controller;

import com.sefault.server.finance.dto.record.PayrollRecord;
import com.sefault.server.finance.service.PayrollService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@RequestMapping("/api/v1/finance/payroll")
@RequiredArgsConstructor
@Tag(name = "Finance - Payroll")
public class PayrollController {
    private final PayrollService payrollService;

    @Operation(
            summary = "Process employee payroll",
            description =
                    "Calculates commissions based on sales within the specified date range and creates a secured payroll transaction.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Payroll processed successfully"),
            @ApiResponse(responseCode = "404", description = "Employee not found")
    })
    @PostMapping("/process/{employeeId}")
    @PreAuthorize("hasAuthority(@authorities.processPayrollAuthority)")
    public ResponseEntity<PayrollRecord> processPayroll(
            @PathVariable UUID employeeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        return ResponseEntity.ok(payrollService.processPayroll(employeeId, startDate, endDate));
    }

    @Operation(summary = "Get payroll details", description = "Fetch a specific payroll run by its UUID.")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority(@authorities.readPayrollAuthority)")
    public ResponseEntity<PayrollRecord> getPayroll(@PathVariable UUID id) {
        return ResponseEntity.ok(payrollService.getPayroll(id));
    }

    @Operation(
            summary = "Get all payroll history",
            description = "Fetch a paginated list of all company payroll records.")
    @GetMapping
    @PreAuthorize("hasAuthority(@authorities.readPayrollAuthority)")
    public ResponseEntity<Page<PayrollRecord>> getAllPayrolls(@PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(payrollService.getAllPayrolls(pageable));
    }

    @Operation(
            summary = "Get payroll by employee",
            description = "Fetch the paginated payroll history for a specific employee.")
    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAuthority(@authorities.readPayrollAuthority)")
    public ResponseEntity<Page<PayrollRecord>> getPayrollHistoryForEmployee(
            @PathVariable UUID employeeId, @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(payrollService.getPayrollHistoryForEmployee(employeeId, pageable));
    }
}
