package com.sefault.server.hr.controller;

import com.sefault.server.hr.dto.record.EmployeeRecord;
import com.sefault.server.hr.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/employee")
@RequiredArgsConstructor
public class EmployeeController {
    private final EmployeeService employeeService;

    @PostMapping
    public ResponseEntity<EmployeeRecord> create(@RequestBody EmployeeRecord employeeRecord){
        return ResponseEntity.ok(employeeService.create(employeeRecord));
    }

    @GetMapping
    public ResponseEntity<List<EmployeeRecord>> getAll() {
        return ResponseEntity.ok(employeeService.getAll());
    }

    @GetMapping("/active")
    public ResponseEntity<List<EmployeeRecord>> getActive() {
        return ResponseEntity.ok(employeeService.getActive());
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeRecord> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(employeeService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EmployeeRecord> update(@PathVariable UUID id, @RequestBody EmployeeRecord record) {
        return ResponseEntity.ok(employeeService.update(id, record));
    }

    @PatchMapping("/{id}/terminate")
    public ResponseEntity<EmployeeRecord> terminate(@PathVariable UUID id) {
        return ResponseEntity.ok(employeeService.terminate(id));
    }

    @DeleteMapping("/{id}")
    public void delete(@PathVariable UUID id) {
        employeeService.delete(id);
    }
}
