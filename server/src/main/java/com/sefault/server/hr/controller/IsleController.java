package com.sefault.server.hr.controller;

import com.sefault.server.hr.dto.record.IsleRecord;
import com.sefault.server.hr.service.IsleService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/isle")
@RequiredArgsConstructor
public class IsleController {
    private final IsleService isleService;

    @PostMapping
    public ResponseEntity<IsleRecord> create(@RequestBody IsleRecord record) {
        return ResponseEntity.ok(isleService.create(record));
    }

    @GetMapping
    public ResponseEntity<List<IsleRecord>> getAll() {
        return ResponseEntity.ok(isleService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<IsleRecord> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(isleService.getById(id));
    }

    @GetMapping("/employee/{employeeId}")
    public ResponseEntity<List<IsleRecord>> getByEmployee(@PathVariable UUID employeeId) {
        return ResponseEntity.ok(isleService.getByEmployee(employeeId));
    }

    @PutMapping("/{id}")
    public ResponseEntity<IsleRecord> update(@PathVariable UUID id, @RequestBody IsleRecord record) {
        return ResponseEntity.ok(isleService.update(id, record));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        isleService.delete(id);
    }

}
