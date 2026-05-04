package com.sefault.server.hr.controller;

import com.sefault.server.hr.dto.record.IsleRecord;
import com.sefault.server.hr.service.IsleService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/isle")
@RequiredArgsConstructor
public class IsleController {
    private final IsleService isleService;

    @PostMapping
    @PreAuthorize("hasAuthority(@authorities.createIsleAuthority)")
    public ResponseEntity<IsleRecord> create(@Valid @RequestBody IsleRecord record) {
        return ResponseEntity.ok(isleService.create(record));
    }

    @GetMapping
    @PreAuthorize("hasAuthority(@authorities.listIslesAuthority)")
    public ResponseEntity<Page<IsleRecord>> getAll(Pageable pageable) {
        return ResponseEntity.ok(isleService.getAll(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority(@authorities.getIsleAuthority)")
    public ResponseEntity<IsleRecord> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(isleService.getById(id));
    }

    @GetMapping("/employee/{employeeId}")
    @PreAuthorize("hasAuthority(@authorities.listIslesAuthority)")
    public ResponseEntity<List<IsleRecord>> getByEmployee(@PathVariable UUID employeeId) {
        return ResponseEntity.ok(isleService.getByEmployee(employeeId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority(@authorities.updateIsleAuthority)")
    public ResponseEntity<IsleRecord> update(@PathVariable UUID id, @Valid @RequestBody IsleRecord record) {
        return ResponseEntity.ok(isleService.update(id, record));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority(@authorities.deleteIsleAuthority)")
    public void delete(@PathVariable UUID id) {
        isleService.delete(id);
    }
}
