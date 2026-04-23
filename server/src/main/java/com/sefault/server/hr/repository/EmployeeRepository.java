package com.sefault.server.hr.repository;

import com.sefault.server.hr.dto.projection.EmployeeProjection;
import com.sefault.server.hr.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import lombok.NonNull;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmployeeRepository extends JpaRepository<@NonNull Employee, @NonNull UUID> {
    Optional<EmployeeProjection> getEmployeeProjectionById(UUID id);
}