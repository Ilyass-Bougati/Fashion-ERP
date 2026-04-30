package com.sefault.server.hr.controller;

import com.sefault.server.hr.dto.record.EmployeeRecord;
import com.sefault.server.hr.service.EmployeeService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/employee")
@RequiredArgsConstructor
public class EmployeeController {
    private final EmployeeService employeeService;

    @PostMapping
    @PreAuthorize("hasAuthority(@authorities.createEmployeeAuthority)")
    public ResponseEntity<EmployeeRecord> create(@RequestBody EmployeeRecord employeeRecord) {
        return ResponseEntity.ok(employeeService.create(employeeRecord));
    }

    @GetMapping
    @PreAuthorize("hasAuthority(@authorities.listEmployeesAuthority)")
    public ResponseEntity<List<EmployeeRecord>> getAll() {
        return ResponseEntity.ok(employeeService.getAll());
    }

    @GetMapping("/active")
    @PreAuthorize("hasAuthority(@authorities.listEmployeesAuthority)")
    public ResponseEntity<List<EmployeeRecord>> getActive() {
        return ResponseEntity.ok(employeeService.getActive());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority(@authorities.getEmployeeAuthority)")
    public ResponseEntity<EmployeeRecord> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(employeeService.getById(id));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority(@authorities.updateEmployeeAuthority)")
    public ResponseEntity<EmployeeRecord> update(@PathVariable UUID id, @RequestBody EmployeeRecord record) {
        return ResponseEntity.ok(employeeService.update(id, record));
    }

    @PatchMapping("/{id}/terminate")
    @PreAuthorize("hasAuthority(@authorities.terminateEmployeeAuthority)")
    public ResponseEntity<EmployeeRecord> terminate(@PathVariable UUID id) {
        return ResponseEntity.ok(employeeService.terminate(id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority(@authorities.deleteEmployeeAuthority)")
    public void delete(@PathVariable UUID id) {
        employeeService.delete(id);
    }
}
