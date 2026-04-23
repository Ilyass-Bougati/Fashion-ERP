package com.sefault.server.user.repository;

import com.sefault.server.user.dto.projection.UserReportProjection;
import com.sefault.server.user.entity.UserReport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import lombok.NonNull;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserReportRepository extends JpaRepository<@NonNull UserReport, @NonNull UUID> {
    Optional<UserReportProjection> getUserReportProjectionById(UUID id);
}