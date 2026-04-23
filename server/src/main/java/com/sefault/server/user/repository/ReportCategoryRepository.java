package com.sefault.server.user.repository;

import com.sefault.server.user.dto.projection.ReportCategoryProjection;
import com.sefault.server.user.entity.ReportCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import lombok.NonNull;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReportCategoryRepository extends JpaRepository<@NonNull ReportCategory, @NonNull UUID> {
    Optional<ReportCategoryProjection> getReportCategoryProjectionById(UUID id);
}