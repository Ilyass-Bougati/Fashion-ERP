package com.sefault.server.hr.service.Impl;

import com.sefault.server.exception.NotFoundException;
import com.sefault.server.hr.dto.record.EmployeeRecord;
import com.sefault.server.hr.entity.Employee;
import com.sefault.server.hr.mapper.EmployeeMapper;
import com.sefault.server.hr.repository.EmployeeRepository;
import com.sefault.server.hr.service.EmployeeService;
import com.sefault.server.image.repository.ImageRepository;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class EmployeeServiceImpl implements EmployeeService {
    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;
    private final ImageRepository imageRepository;

    @Override
    public EmployeeRecord create(EmployeeRecord record) {

        Employee employee = employeeMapper.toEntity(record);

        employee.setImage(imageRepository.getReferenceById(record.imageId()));

        Employee saved = employeeRepository.save(employee);

        return employeeMapper.entityToRecord(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeRecord getById(UUID id) {
        return employeeRepository
                .getEmployeeProjectionById(id)
                .map(employeeMapper::projectionToRecord)
                .orElseThrow(() -> new NotFoundException("Employee not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EmployeeRecord> getAll(Pageable pageable) {
        return employeeRepository.findAllBy(pageable).map(employeeMapper::projectionToRecord);
    }

    @Override
    public void delete(UUID id) {
        employeeRepository.deleteById(id);
    }

    @Override
    public EmployeeRecord update(UUID id, EmployeeRecord record) {
        Employee employee = findEntityOrThrow(id);

        employeeMapper.updateEntityFromRecord(record, employee);

        employee.setImage(imageRepository.getReferenceById(record.imageId()));

        return employeeMapper.entityToRecord(employeeRepository.save(employee));
    }

    @Override
    public Page<EmployeeRecord> getActive(Pageable pageable) {
        return employeeRepository.findAllByActiveTrue(pageable).map(employeeMapper::projectionToRecord);
    }

    @Override
    public Page<EmployeeRecord> getTerminated(Pageable pageable) {
        return employeeRepository.findAllByActiveFalse(pageable).map(employeeMapper::projectionToRecord);
    }

    @Override
    public EmployeeRecord terminate(UUID id) {
        int updatedRows = employeeRepository.terminateEmployee(id, LocalDateTime.now());
        if (updatedRows == 0) {
            throw new NotFoundException("Employee not found with id: " + id);
        }

        return getById(id);
    }

    private Employee findEntityOrThrow(UUID id) {
        return employeeRepository
                .findById(id)
                .orElseThrow(() -> new NotFoundException("Employee not found with id: " + id));
    }
}
