package com.sefault.server.sales.controller;

import com.sefault.server.sales.dto.record.SaleLineRecord;
import com.sefault.server.sales.entity.id.SaleLineId;
import com.sefault.server.sales.service.SaleLineService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/sale-line")
@RequiredArgsConstructor
public class SaleLineController {

    private final SaleLineService saleLineService;

    @PostMapping
    @PreAuthorize("hasAuthority(@authorities.createSaleLineAuthority)")
    public ResponseEntity<SaleLineRecord> create(@Valid @RequestBody SaleLineRecord record) {
        return ResponseEntity.ok(saleLineService.create(record));
    }

    @GetMapping("/sale/{saleId}")
    @PreAuthorize("hasAuthority(@authorities.readSaleLineAuthority)")
    public ResponseEntity<List<SaleLineRecord>> getBySaleId(@PathVariable UUID saleId) {
        return ResponseEntity.ok(saleLineService.getBySaleId(saleId));
    }

    @GetMapping("/{saleId}/{productVariationId}")
    @PreAuthorize("hasAuthority(@authorities.readSaleLineAuthority)")
    public ResponseEntity<SaleLineRecord> getById(@PathVariable UUID saleId, @PathVariable UUID productVariationId) {
        return ResponseEntity.ok(saleLineService.getById(new SaleLineId(saleId, productVariationId)));
    }

    @PutMapping("/{saleId}/{productVariationId}")
    @PreAuthorize("hasAuthority(@authorities.updateSaleLineAuthority)")
    public ResponseEntity<SaleLineRecord> update(
            @PathVariable UUID saleId,
            @PathVariable UUID productVariationId,
            @Valid @RequestBody SaleLineRecord record) {
        return ResponseEntity.ok(saleLineService.update(new SaleLineId(saleId, productVariationId), record));
    }

    @DeleteMapping("/{saleId}/{productVariationId}")
    @PreAuthorize("hasAuthority(@authorities.deleteSaleLineAuthority)")
    public void delete(@PathVariable UUID saleId, @PathVariable UUID productVariationId) {
        saleLineService.delete(new SaleLineId(saleId, productVariationId));
    }
}
