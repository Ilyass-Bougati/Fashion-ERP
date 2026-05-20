package com.sefault.server.hr.repository;

import com.sefault.server.hr.dto.projection.EmployeeProjection;
import com.sefault.server.hr.entity.Employee;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;
import lombok.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeRepository extends JpaRepository<@NonNull Employee, @NonNull UUID> {
    Optional<EmployeeProjection> getEmployeeProjectionById(UUID id);

    Page<EmployeeProjection> findAllBy(Pageable pageable);

    Page<EmployeeProjection> findAllByActiveTrue(Pageable pageable);

    Page<EmployeeProjection> findAllByActiveFalse(Pageable pageable);

    @Modifying(clearAutomatically = true)
    @Query("UPDATE Employee e SET e.active = false, e.terminatedAt = :terminatedAt WHERE e.id = :id")
    int terminateEmployee(@Param("id") UUID id, @Param("terminatedAt") LocalDateTime terminatedAt);

    Integer countByActiveTrue();
}
