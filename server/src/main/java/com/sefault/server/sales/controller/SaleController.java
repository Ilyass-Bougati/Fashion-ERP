package com.sefault.server.sales.controller;

import com.sefault.server.sales.dto.record.SaleRecord;
import com.sefault.server.sales.service.SaleService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import com.sefault.server.finance.dto.record.TransactionRecord;

@RestController
@RequestMapping("/api/v1/sale")
@RequiredArgsConstructor
public class SaleController {

    private final SaleService saleService;

    @PostMapping
    @PreAuthorize("hasAuthority(@authorities.createSaleAuthority)")
    public ResponseEntity<SaleRecord> create(@Valid @RequestBody SaleRecord record) {
        return ResponseEntity.ok(saleService.create(record));
    }

    @GetMapping
    @PreAuthorize("hasAuthority(@authorities.readSaleAuthority)")
    public ResponseEntity<Page<SaleRecord>> getAll(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(saleService.getAll(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority(@authorities.readSaleAuthority)")
    public ResponseEntity<SaleRecord> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(saleService.getById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority(@authorities.updateSaleAuthority)")
    public ResponseEntity<SaleRecord> update(@PathVariable UUID id, @Valid @RequestBody SaleRecord record) {
        return ResponseEntity.ok(saleService.update(id, record));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority(@authorities.deleteSaleAuthority)")
    public void delete(@PathVariable UUID id) {
        saleService.delete(id);
    }

    @PostMapping("/{id}/checkout")
    @PreAuthorize("hasAuthority(@authorities.createTransactionAuthority)")
    public ResponseEntity<TransactionRecord> checkout(@PathVariable UUID id) {
        return ResponseEntity.ok(saleService.checkout(id));
    }
}
