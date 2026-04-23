package com.sefault.server.hr.repository;

import com.sefault.server.hr.dto.projection.IsleProjection;
import com.sefault.server.hr.entity.Isle;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import lombok.NonNull;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface IsleRepository extends JpaRepository<@NonNull Isle, @NonNull UUID> {
    Optional<IsleProjection> getIsleProjectionById(UUID id);
}