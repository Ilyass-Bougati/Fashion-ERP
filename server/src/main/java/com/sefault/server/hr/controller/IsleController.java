package com.sefault.server.hr.controller;

import com.sefault.server.hr.dto.record.IsleRecord;
import com.sefault.server.hr.service.IsleService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
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
    public ResponseEntity<IsleRecord> create(@RequestBody IsleRecord record) {
        return ResponseEntity.ok(isleService.create(record));
    }

    @GetMapping
    @PreAuthorize("hasAuthority(@authorities.listIslesAuthority)")
    public ResponseEntity<List<IsleRecord>> getAll() {
        return ResponseEntity.ok(isleService.getAll());
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
    public ResponseEntity<IsleRecord> update(@PathVariable UUID id, @RequestBody IsleRecord record) {
        return ResponseEntity.ok(isleService.update(id, record));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority(@authorities.deleteIsleAuthority)")
    public void delete(@PathVariable UUID id) {
        isleService.delete(id);
    }

    @PatchMapping("/{isleId}/assign/{employeeId}")
    @PreAuthorize("hasAuthority(@authorities.assignIsleAuthority)")
    public ResponseEntity<IsleRecord> assignEmployee(@PathVariable UUID isleId, @PathVariable UUID employeeId) {
        return ResponseEntity.ok(isleService.assignEmployee(isleId, employeeId));
    }

    @PatchMapping("/{isleId}/unassign")
    @PreAuthorize("hasAuthority(@authorities.assignIsleAuthority)")
    public ResponseEntity<IsleRecord> unassignEmployee(@PathVariable UUID isleId) {
        return ResponseEntity.ok(isleService.unassignEmployee(isleId));
    }
}
