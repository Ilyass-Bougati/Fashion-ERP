package com.sefault.server.hr.service.Impl;

import com.sefault.server.exception.NotFoundException;
import com.sefault.server.hr.dto.record.IsleRecord;
import com.sefault.server.hr.entity.Isle;
import com.sefault.server.hr.mapper.IsleMapper;
import com.sefault.server.hr.repository.EmployeeRepository;
import com.sefault.server.hr.repository.IsleRepository;
import com.sefault.server.hr.service.IsleService;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

        Isle isle = isleMapper.toEntity(isleRecord);

        isle.setEmployee(employeeRepository.getReferenceById(isleRecord.employeeId()));

        return isleMapper.entityToRecord(isleRepository.save(isle));
    }

    @Override
    @Transactional(readOnly = true)
    public IsleRecord getById(UUID id) {
        return isleRepository
                .getIsleProjectionById(id)
                .map(isleMapper::projectionToRecord)
                .orElseThrow(() -> new NotFoundException("Isle not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<IsleRecord> getAll(Pageable pageable) {
        return isleRepository.findAllBy(pageable).map(isleMapper::projectionToRecord);
    }

    @Override
    public void delete(UUID id) {
        isleRepository.deleteById(id);
    }

    @Override
    public IsleRecord update(UUID id, IsleRecord isleRecord) {
        Isle isle = findEntityOrThrow(id);

        isleMapper.updateEntityFromRecord(isleRecord, isle);

        isle.setEmployee(employeeRepository.getReferenceById(isleRecord.employeeId()));

        return isleMapper.entityToRecord(isleRepository.save(isle));
    }

    @Override
    @Transactional(readOnly = true)
    public List<IsleRecord> getByEmployee(UUID id) {
        return isleRepository.findAllByEmployeeId(id).stream()
                .map(isleMapper::projectionToRecord)
                .toList();
    }

    private Isle findEntityOrThrow(UUID id) {
        return isleRepository.findById(id).orElseThrow(() -> new NotFoundException("Isle not found with id: " + id));
    }
}
