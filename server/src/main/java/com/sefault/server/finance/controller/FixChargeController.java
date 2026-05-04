package com.sefault.server.finance.controller;

import com.sefault.server.finance.dto.record.FixChargeRecord;
import com.sefault.server.finance.service.FixChargeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/finance/fixed-charges")
@RequiredArgsConstructor
@Tag(name = "Finance - Fixed Charges")
public class FixChargeController {
    private final FixChargeService fixChargeService;

    @Operation(
            summary = "Create a fixed charge",
            description = "Register a new recurring charge (e.g., Rent, Software).")
    @PostMapping
    @PreAuthorize("hasAuthority(@authorities.createFixedChargeAuthority)")
    public ResponseEntity<FixChargeRecord> createFixCharge(@RequestBody @Valid FixChargeRecord record) {
        return ResponseEntity.ok(fixChargeService.createFixCharge(record));
    }

    @Operation(summary = "Get fixed charge details", description = "Fetch a specific fixed charge by its UUID.")
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority(@authorities.readFixedChargeAuthority)")
    public ResponseEntity<FixChargeRecord> getFixCharge(@PathVariable UUID id) {
        return ResponseEntity.ok(fixChargeService.getFixCharge(id));
    }

    @Operation(
            summary = "Update a fixed charge",
            description = "Update the details or amount of an existing fixed charge.")
    @PutMapping
    @PreAuthorize("hasAuthority(@authorities.updateFixedChargeAuthority)")
    public ResponseEntity<FixChargeRecord> updateFixCharge(@RequestBody @Valid FixChargeRecord record) {
        return ResponseEntity.ok(fixChargeService.updateFixCharge(record));
    }

    @Operation(summary = "Toggle charge status", description = "Activate or deactivate a fixed charge.")
    @ApiResponse(responseCode = "204", description = "Status toggled successfully")
    @PatchMapping("/{id}/toggle")
    @PreAuthorize("hasAuthority(@authorities.toggleFixedChargeAuthority)")
    public ResponseEntity<Void> toggleFixChargeStatus(@PathVariable UUID id) {
        fixChargeService.toggleFixChargeStatus(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "Get all fixed charges",
            description = "Fetch a paginated list of fixed charges, with an optional filter for active ones only.")
    @GetMapping
    @PreAuthorize("hasAuthority(@authorities.readFixedChargeAuthority)")
    public ResponseEntity<Page<FixChargeRecord>> getAllFixCharges(
            @RequestParam(required = false, defaultValue = "false") boolean activeOnly,
            @PageableDefault(size = 20) Pageable pageable) {

        if (activeOnly) {
            return ResponseEntity.ok(fixChargeService.getActiveFixCharges(pageable));
        }
        return ResponseEntity.ok(fixChargeService.getAllFixCharges(pageable));
    }
}
