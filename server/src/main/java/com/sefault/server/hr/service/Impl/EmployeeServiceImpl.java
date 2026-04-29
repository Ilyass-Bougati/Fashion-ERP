package com.sefault.server.hr.service.Impl;

import com.sefault.server.hr.dto.projection.EmployeeProjection;
import com.sefault.server.hr.dto.record.EmployeeRecord;
import com.sefault.server.hr.entity.Employee;
import com.sefault.server.hr.mapper.EmployeeMapper;
import com.sefault.server.hr.repository.EmployeeRepository;
import com.sefault.server.hr.service.EmployeeService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeServiceImpl implements EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;

    @Override
    public EmployeeRecord create(EmployeeRecord record) {
        if(employeeRepository.existsByEmail(record.email())){
            throw new IllegalArgumentException("Email already exists");
        }
        if (employeeRepository.existsByCIN(record.CIN())){
            throw new IllegalArgumentException("CIN already exists");
        }
        if (employeeRepository.existsByPhoneNumber(record.phoneNumber())){
            throw new IllegalArgumentException("Phone number already exists");
        }

        Employee employee = employeeMapper.toEntity(record);
        Employee saved = employeeRepository.save(employee);

        return employeeMapper.entityToRecord(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeRecord getById(UUID id){
        return employeeRepository.getEmployeeProjectionById(id)
                .map(employeeMapper::projectionToRecord)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeRecord> getAll(){
        return employeeRepository.findAllBy()
                .stream()
                .map(employeeMapper::projectionToRecord)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(UUID id){
        employeeRepository.deleteById(id);
    }

    @Override
    public EmployeeRecord update(UUID id, EmployeeRecord record) {
        Employee employee = findEntityOrThrow(id);
        if(employeeRepository.existsByCINAndIdNot(record.CIN(), id)){
            throw new IllegalArgumentException("CIN already exists");
        }
        if(employeeRepository.existsByPhoneNumberAndIdNot(record.phoneNumber(), id)){
            throw new IllegalArgumentException("Phone number already exists");
        }
        if (employeeRepository.existsByEmailAndIdNot(record.email(), id)){
            throw new IllegalArgumentException("Email already exists");
        }

        employeeMapper.updateEntityFromRecord(record, employee);
        return employeeMapper.entityToRecord(employeeRepository.save(employee));
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeRecord> getActive(){
        return employeeRepository.findAllBy()
                .stream()
                .filter(p->Boolean.TRUE.equals(p.getActive()))
                .map(employeeMapper::projectionToRecord)
                .collect(Collectors.toList());
    }

    @Override
    public EmployeeRecord terminate(UUID id) {
        Employee employee = findEntityOrThrow(id);
        employee.setActive(false);
        employee.setTerminatedAt(LocalDateTime.now());
        return employeeMapper.entityToRecord(employeeRepository.save(employee));
    }

    private Employee findEntityOrThrow(UUID id) {
        return employeeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Employee not found with id: " + id));
    }
}