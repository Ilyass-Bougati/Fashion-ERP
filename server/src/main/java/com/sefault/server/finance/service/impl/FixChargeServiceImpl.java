package com.sefault.server.finance.service.impl;

import com.sefault.server.finance.dto.record.FixChargeRecord;
import com.sefault.server.finance.entity.FixCharge;
import com.sefault.server.finance.mapper.FixChargeMapper;
import com.sefault.server.finance.repository.FixChargeRepository;
import com.sefault.server.finance.service.FixChargeService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class FixChargeServiceImpl implements FixChargeService {
    private final FixChargeRepository fixChargeRepository;
    private final FixChargeMapper fixChargeMapper;

    @Override
    public FixChargeRecord createFixCharge(FixChargeRecord fixChargeRecord) {
        FixCharge fixCharge = fixChargeMapper.toEntity(fixChargeRecord);
        return fixChargeMapper.entityToRecord(fixChargeRepository.saveAndFlush(fixCharge));
    }

    @Override
    @Transactional(readOnly = true)
    public FixChargeRecord getFixCharge(UUID chargeId) {
        return fixChargeRepository
                .getFixChargeProjectionById(chargeId)
                .map(fixChargeMapper::projectionToRecord)
                .orElseThrow(() -> new EntityNotFoundException("FixCharge not found"));
    }

    @Override
    public FixChargeRecord updateFixCharge(FixChargeRecord fixChargeRecord) {
        FixCharge existingCharge = fixChargeRepository
                .findById(fixChargeRecord.id())
                .orElseThrow(() -> new EntityNotFoundException("FixCharge not found"));

        fixChargeMapper.updateEntityFromRecord(fixChargeRecord, existingCharge);

        return fixChargeMapper.entityToRecord(fixChargeRepository.save(existingCharge));
    }

    @Override
    public void toggleFixChargeStatus(UUID chargeId) {
        FixCharge existingCharge = fixChargeRepository
                .findById(chargeId)
                .orElseThrow(() -> new EntityNotFoundException("FixCharge not found"));

        existingCharge.setActive(!existingCharge.getActive());
        fixChargeRepository.save(existingCharge);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FixChargeRecord> getAllFixCharges(Pageable pageable) {
        return fixChargeRepository.findAllBy(pageable).map(fixChargeMapper::projectionToRecord);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<FixChargeRecord> getActiveFixCharges(Pageable pageable) {
        return fixChargeRepository.findByActiveTrue(pageable).map(fixChargeMapper::projectionToRecord);
    }
}
