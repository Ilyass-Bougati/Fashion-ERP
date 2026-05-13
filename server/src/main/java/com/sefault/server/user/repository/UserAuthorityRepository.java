package com.sefault.server.user.repository;

import com.sefault.server.user.dto.projection.UserAuthorityProjection;
import com.sefault.server.user.entity.UserAuthority;
import com.sefault.server.user.entity.id.UserAuthorityId;
import java.util.Optional;
import java.util.UUID;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface UserAuthorityRepository extends JpaRepository<@NonNull UserAuthority, @NonNull UserAuthorityId> {
    Optional<UserAuthorityProjection> getUserAuthorityProjectionById(UserAuthorityId id);

    @Modifying
    @Query("DELETE FROM UserAuthority ua WHERE ua.user.id = :userId AND ua.authority.id = :authorityId")
    void deleteByUser_IdAndAuthority_Id(@Param("userId") UUID userId, @Param("authorityId") UUID authorityId);
}
