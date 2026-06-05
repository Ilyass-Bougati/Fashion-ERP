package com.sefault.server.user.repository;

import com.sefault.server.user.dto.projection.UserReportProjection;
import com.sefault.server.user.entity.UserReport;
import com.sefault.server.user.entity.id.UserReportId;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserReportRepository extends JpaRepository<@NonNull UserReport, @NonNull UserReportId> {
    Page<UserReportProjection> findAllBy(Pageable pageable);
}
