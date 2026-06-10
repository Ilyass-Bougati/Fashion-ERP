package com.sefault.server.storage.controller;

import com.sefault.server.storage.dto.record.VendorRecord;
import com.sefault.server.storage.service.VendorService;
import jakarta.validation.Valid;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/vendors")
@RequiredArgsConstructor
public class VendorController {
    private final VendorService vendorService;

    @GetMapping
    @PreAuthorize("hasAuthority(@authorities.listVendorsAuthority)")
    public Page<VendorRecord> getAll(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return vendorService.findAllPaginated(pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority(@authorities.getVendorAuthority)")
    public VendorRecord getById(@PathVariable UUID id) {
        return vendorService.getById(id);
    }

    @PostMapping
    @PreAuthorize("hasAuthority(@authorities.createVendorAuthority)")
    public VendorRecord save(@RequestBody VendorRecord vendorRecord) {
        return vendorService.save(vendorRecord);
    }

    @PutMapping
    @PreAuthorize("hasAuthority(@authorities.updateVendorAuthority)")
    public VendorRecord update(@Valid @RequestBody VendorRecord vendorRecord) {
        return vendorService.update(vendorRecord.id(), vendorRecord);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority(@authorities.deleteVendorAuthority)")
    public void delete(@PathVariable UUID id) {
        vendorService.delete(id);
    }
}
