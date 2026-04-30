package com.sefault.server.finance.service;

import com.sefault.server.finance.dto.record.FixChargeRecord;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface FixChargeService {
    FixChargeRecord createFixCharge(FixChargeRecord fixChargeRecord);

    FixChargeRecord getFixCharge(UUID chargeId);

    FixChargeRecord updateFixCharge(FixChargeRecord fixChargeRecord);

    void toggleFixChargeStatus(UUID chargeId);

    Page<FixChargeRecord> getAllFixCharges(Pageable pageable);

    Page<FixChargeRecord> getActiveFixCharges(Pageable pageable);
}
