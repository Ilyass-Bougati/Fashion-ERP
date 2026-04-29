package com.sefault.server.hr.service.Impl;

import com.sefault.server.hr.dto.record.IsleRecord;
import com.sefault.server.hr.entity.Isle;
import com.sefault.server.hr.mapper.IsleMapper;
import com.sefault.server.hr.repository.IsleRepository;
import com.sefault.server.hr.service.IsleService;
import jakarta.persistence.EntityNotFoundException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class IsleServiceImpl implements IsleService {
    private final IsleRepository isleRepository;
    private final IsleMapper isleMapper;

    @Override
    public IsleRecord create(IsleRecord isleRecord) {
        if(isleRepository.existsByCode(isleRecord.code())){
            throw new IllegalArgumentException("Isle code already exists: " + isleRecord.code());
        }


        Isle isle = isleMapper.toEntity(isleRecord);
        return isleMapper.entityToRecord(isleRepository.save(isle));
    }

    @Override
    @Transactional(readOnly = true)
    public IsleRecord getById(UUID id){
        return isleRepository.getIsleProjectionById(id)
                .map(isleMapper::projectionToRecord)
                .orElseThrow(() -> new EntityNotFoundException("Isle not found with id: " + id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<IsleRecord> getAll() {
        return isleRepository.findAllBy()
                .stream()
                .map(isleMapper::projectionToRecord)
                .collect(Collectors.toList());
    }

    @Override
    public void delete(UUID id) {
        isleRepository.deleteById(id);
    }

    @Override
    public IsleRecord update(UUID id, IsleRecord isleRecord) {
        Isle isle = findEntityOrThrow(id);
        if(isleRepository.existsByCodeAndIdNot(isleRecord.code(), id)){
            throw new IllegalArgumentException("Isle code already exists: " + isleRecord.code());
        }

        isleMapper.updateEntityFromRecord(isleRecord, isle);

        return isleMapper.entityToRecord(isleRepository.save(isle));

    }

    @Override
    @Transactional(readOnly = true)
    public List<IsleRecord> getByEmployee(UUID id) {
        return isleRepository.findAllByEmployeeId(id)
                .stream()
                .map(isleMapper::projectionToRecord)
                .collect(Collectors.toList());
    }

    private Isle findEntityOrThrow(UUID id) {
        return isleRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Isle not found with id: " + id));
    }

}
