package com.sefault.server.finance.repository;

import com.sefault.server.finance.dto.projection.FixChargeProjection;
import com.sefault.server.finance.entity.FixCharge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import lombok.NonNull;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FixChargeRepository extends JpaRepository<@NonNull FixCharge, @NonNull UUID> {
    Optional<FixChargeProjection> getFixChargeProjectionById(UUID id);
}