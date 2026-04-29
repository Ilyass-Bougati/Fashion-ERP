package com.sefault.server.hr.repository;

import com.sefault.server.hr.dto.projection.EmployeeProjection;
import com.sefault.server.hr.entity.Employee;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeRepository extends JpaRepository<@NonNull Employee, @NonNull UUID> {
    Optional<EmployeeProjection> getEmployeeProjectionById(UUID id);

    boolean existsByPhoneNumber(String phoneNumber);

    boolean existsByCIN(String CIN);

    boolean existsByEmail(String email);

    List<EmployeeProjection> findAllBy();

    boolean existsByPhoneNumberAndIdNot(String phoneNumber, UUID id);
    boolean existsByCINAndIdNot(String CIN, UUID id);
    boolean existsByEmailAndIdNot(String email, UUID id);

}
