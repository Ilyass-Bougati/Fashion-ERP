package com.sefault.server.user.repository;

import com.sefault.server.user.dto.projection.UserProjection;
import com.sefault.server.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import lombok.NonNull;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<@NonNull User, @NonNull UUID> {
    Optional<UserProjection> getUserProjectionById(UUID id);
}