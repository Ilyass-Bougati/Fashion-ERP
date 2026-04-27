package com.sefault.server.user.repository;

import com.sefault.server.user.dto.projection.SessionProjection;
import com.sefault.server.user.entity.Session;
import io.lettuce.core.dynamic.annotation.Param;
import java.util.Optional;
import java.util.UUID;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface SessionRepository extends JpaRepository<@NonNull Session, @NonNull UUID> {
    Optional<SessionProjection> getSessionProjectionById(UUID id);

    @Modifying
    @Query("UPDATE Session s SET s.active = false, s.closedAt = CURRENT_TIMESTAMP WHERE s.id = :sessionId")
    void endById(@Param("sessionId") UUID sessionId);
}
