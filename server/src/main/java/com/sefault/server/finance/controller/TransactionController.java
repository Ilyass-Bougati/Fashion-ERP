package com.sefault.server.finance.controller;

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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/finance/transactions")
@RequiredArgsConstructor
@Tag(name = "Finance - Transactions")
public class TransactionController {
    private final FinanceService financeService;

    @Operation(
            summary = "Create a manual transaction",
            description = "Creates a generic inflow or outflow transaction.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transaction created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid payload"),
            @ApiResponse(responseCode = "403", description = "Insufficient authorities")
    })
    @PostMapping
    @PreAuthorize("hasAuthority(@authorities.createTransactionAuthority)")
    public ResponseEntity<TransactionRecord> createTransactionRecord(
            @RequestBody @Valid TransactionRecord transactionRecord) {
        return ResponseEntity.ok(financeService.createTransaction(transactionRecord));
    }

    @Operation(summary = "Get transaction details", description = "Fetch a specific transaction by its UUID.")
    @ApiResponse(responseCode = "404", description = "Transaction not found")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority(@authorities.readTransactionAuthority)")
    public ResponseEntity<TransactionRecord> getTransaction(@PathVariable UUID id) {
        return ResponseEntity.ok(financeService.getTransaction(id));
    }

    @Operation(
            summary = "Reverse a transaction",
            description =
                    "Creates a counter-balancing transaction (RECEIVED for PAID, and vice versa) to correct ledger mistakes or refunding.")
    @PostMapping("/{id}/reverse")
    @PreAuthorize("hasAuthority(@authorities.reverseTransactionAuthority)")
    public ResponseEntity<TransactionRecord> reverseTransaction(@PathVariable UUID id) {
        return ResponseEntity.ok(financeService.reverseTransaction(id));
    }

    @Operation(
            summary = "Get all transactions",
            description = "Fetch a paginated ledger of transactions. Can optionally be filtered by transaction type.")
    @GetMapping
    @PreAuthorize("hasAuthority(@authorities.readTransactionAuthority)")
    public ResponseEntity<Page<TransactionRecord>> getAllTransactions(
            @RequestParam(required = false) TransactionType type, @PageableDefault(size = 20) Pageable pageable) {
        if (type != null) {
            return ResponseEntity.ok(financeService.getTransactionsByType(type, pageable));
        }
        return ResponseEntity.ok(financeService.getAllTransactions(pageable));
    }
}
