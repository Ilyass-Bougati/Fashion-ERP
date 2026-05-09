package com.sefault.server.finance.repository;

import com.sefault.server.finance.dto.projection.FixChargeProjection;
import com.sefault.server.finance.entity.FixCharge;
import java.util.Optional;
import java.util.UUID;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface FixChargeRepository extends JpaRepository<@NonNull FixCharge, @NonNull UUID> {
    Optional<FixChargeProjection> getFixChargeProjectionById(UUID id);

    Page<FixChargeProjection> findAllBy(Pageable pageable);

    Page<FixChargeProjection> findByActiveTrue(Pageable pageable);

    @Query("SELECT COALESCE(SUM(fc.amount), 0.0) FROM FixCharge fc WHERE fc.active = true ")
    Double calculateActiveFixCharges();
}
