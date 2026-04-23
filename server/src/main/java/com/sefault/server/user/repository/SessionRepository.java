package com.sefault.server.user.repository;

import com.sefault.server.user.dto.projection.SessionProjection;
import com.sefault.server.user.entity.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import lombok.NonNull;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SessionRepository extends JpaRepository<@NonNull Session, @NonNull UUID> {
    Optional<SessionProjection> getSessionProjectionById(UUID id);
}