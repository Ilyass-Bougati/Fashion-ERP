package com.sefault.server.storage.controller;

import com.sefault.server.storage.dto.projection.VendorProjection;
import com.sefault.server.storage.dto.record.VendorRecord;
import com.sefault.server.storage.service.VendorService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/vendors")
@RequiredArgsConstructor
public class VendorController {
    private final VendorService vendorService;

    @GetMapping
    public Page<VendorProjection> getAll(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return vendorService.findAllPaginated(pageable);
    }

    @GetMapping("/{id}")
    public VendorProjection getById(@PathVariable UUID id) {
        return vendorService.getById(id);
    }

    @PostMapping
    public VendorRecord save(@RequestBody VendorRecord vendorRecord) {
        return vendorService.save(vendorRecord);
    }

    @PutMapping
    public VendorRecord update(@RequestBody VendorRecord vendorRecord) {
        return vendorService.update(vendorRecord);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        vendorService.delete(id);
    }
}
