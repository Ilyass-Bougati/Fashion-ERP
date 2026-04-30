package com.sefault.server.hr.service.Impl;

import com.sefault.server.hr.dto.record.IsleRecord;
import com.sefault.server.hr.entity.Isle;
import com.sefault.server.hr.mapper.IsleMapper;
import com.sefault.server.hr.repository.EmployeeRepository;
import com.sefault.server.hr.repository.IsleRepository;
import com.sefault.server.hr.service.IsleService;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class IsleServiceImpl implements IsleService {
    private final IsleRepository isleRepository;
    private final IsleMapper isleMapper;
    private final EmployeeRepository employeeRepository;

    @Override
    public IsleRecord create(IsleRecord isleRecord) {
        if (isleRepository.existsByCode(isleRecord.code())) {
            throw new IllegalArgumentException("Isle code already exists: " + isleRecord.code());
        }

        Isle isle = isleMapper.toEntity(isleRecord);

        if (isleRecord.employeeId() != null) {
            if (!employeeRepository.existsByIdAndActiveTrue(isleRecord.employeeId()))
                throw new IllegalStateException("Cannot assign a terminated employee to an isle");
            isle.setEmployee(employeeRepository.getReferenceById(isleRecord.employeeId()));
        }

        return isleMapper.entityToRecord(isleRepository.save(isle));
    }

    @Override
    @Transactional(readOnly = true)
    public IsleRecord getById(UUID id) {
        return isleRepository
                .getIsleProjectionById(id)
                .map(isleMapper::projectionToRecord)
                .orElseThrow(() -> new EntityNotFoundException("Isle not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<IsleRecord> getAll() {
        return isleRepository.findAllBy().stream()
                .map(isleMapper::projectionToRecord)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(UUID id) {
        findEntityOrThrow(id);
        isleRepository.deleteById(id);
    }

    @Override
    public IsleRecord update(UUID id, IsleRecord isleRecord) {
        Isle isle = findEntityOrThrow(id);
        if (isleRepository.existsByCodeAndIdNot(isleRecord.code(), id)) {
            throw new IllegalArgumentException("Isle code already exists: " + isleRecord.code());
        }

        isleMapper.updateEntityFromRecord(isleRecord, isle);

        if (!Objects.equals(isle.getEmployeeId(), isleRecord.employeeId())) {
            if (isleRecord.employeeId() != null && !employeeRepository.existsByIdAndActiveTrue(isleRecord.employeeId()))
                throw new IllegalStateException("Cannot assign a terminated employee to an isle");

            isle.setEmployee(
                    isleRecord.employeeId() != null
                            ? employeeRepository.getReferenceById(isleRecord.employeeId())
                            : null);
        }

        return isleMapper.entityToRecord(isleRepository.save(isle));
    }

    @Override
    @Transactional(readOnly = true)
    public List<IsleRecord> getByEmployee(UUID id) {
        return isleRepository.findAllByEmployeeId(id).stream()
                .map(isleMapper::projectionToRecord)
                .collect(Collectors.toList());
    }

    @Override
    public IsleRecord assignEmployee(UUID isleId, UUID employeeId) {
        Isle isle = findEntityOrThrow(isleId);
        if (!employeeRepository.existsByIdAndActiveTrue(employeeId))
            throw new IllegalStateException("Cannot assign a terminated employee to an isle");
        isle.setEmployee(employeeRepository.getReferenceById(employeeId));

        return isleMapper.entityToRecord(isleRepository.save(isle));
    }

    @Override
    public IsleRecord unassignEmployee(UUID isleId) {
        Isle isle = findEntityOrThrow(isleId);
        isle.setEmployee(null);
        return isleMapper.entityToRecord(isleRepository.save(isle));
    }

    private Isle findEntityOrThrow(UUID id) {
        return isleRepository
                .findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Isle not found with id: " + id));
    }
}
