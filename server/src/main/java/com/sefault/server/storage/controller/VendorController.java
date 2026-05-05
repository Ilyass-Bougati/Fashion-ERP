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
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("api/v1/vendors")
@RequiredArgsConstructor
public class VendorController {
    private final VendorService vendorService;

    @GetMapping
    public Page<VendorRecord> getAll(
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return vendorService.findAllPaginated(pageable);
    }

    @GetMapping("/{id}")
    public VendorRecord getById(@PathVariable UUID id) {
        return vendorService.getById(id);
    }

    @PostMapping
    public VendorRecord save(@RequestBody VendorRecord vendorRecord) {
        return vendorService.save(vendorRecord);
    }

    @PutMapping
    public VendorRecord update(@Valid @RequestBody UUID id, @Valid @RequestBody VendorRecord vendorRecord) {
        return vendorService.update(id, vendorRecord);
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        vendorService.delete(id);
    }
}
