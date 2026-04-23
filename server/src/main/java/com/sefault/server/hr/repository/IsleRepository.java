package com.sefault.server.hr.repository;

import com.sefault.server.hr.dto.projection.IsleProjection;
import com.sefault.server.hr.entity.Isle;
import java.util.Optional;
import java.util.UUID;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface IsleRepository extends JpaRepository<@NonNull Isle, @NonNull UUID> {
    Optional<IsleProjection> getIsleProjectionById(UUID id);
}
