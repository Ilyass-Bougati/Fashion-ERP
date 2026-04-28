package com.sefault.server.user.repository;

import com.sefault.server.user.dto.projection.UserAuthorityProjection;
import com.sefault.server.user.entity.UserAuthority;
import java.util.Optional;
import java.util.UUID;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserAuthorityRepository extends JpaRepository<@NonNull UserAuthority, @NonNull UUID> {
    Optional<UserAuthorityProjection> getUserAuthorityProjectionById(UUID id);

    void deleteByUser_IdAndAuthority_Id(UUID userId, UUID authorityId);
}
