package com.sefault.server.user.repository;

import com.sefault.server.user.dto.projection.ReportCategoryProjection;
import com.sefault.server.user.entity.ReportCategory;
import java.util.Optional;
import java.util.UUID;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReportCategoryRepository extends JpaRepository<@NonNull ReportCategory, @NonNull UUID> {
    Optional<ReportCategoryProjection> getReportCategoryProjectionById(UUID id);
}
