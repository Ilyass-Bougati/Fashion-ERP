package com.sefault.server.user.repository;

import com.sefault.server.user.dto.projection.ReportProjection;
import com.sefault.server.user.entity.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import lombok.NonNull;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReportRepository extends JpaRepository<@NonNull Report, @NonNull UUID> {
    Optional<ReportProjection> getReportProjectionById(UUID id);
}