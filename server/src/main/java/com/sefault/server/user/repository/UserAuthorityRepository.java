package com.sefault.server.user.repository;

import com.sefault.server.user.dto.projection.UserAuthorityProjection;
import com.sefault.server.user.entity.UserAuthority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import lombok.NonNull;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserAuthorityRepository extends JpaRepository<@NonNull UserAuthority, @NonNull UUID> {
    Optional<UserAuthorityProjection> getUserAuthorityProjectionById(UUID id);
}