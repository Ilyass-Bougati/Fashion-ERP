package com.sefault.server.user.repository;

import com.sefault.server.user.dto.projection.AuthorityProjection;
import com.sefault.server.user.entity.Authority;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface AuthorityRepository extends JpaRepository<Authority, UUID> {
    <T> T findAuthorityById(UUID id, Class<T> type);

    Authority findByName(String name);

    @Query("SELECT a FROM Authority a JOIN a.userAuthorities ua WHERE ua.user.id = :userId")
    List<AuthorityProjection> getAuthoritiesByUserId(UUID userId);
}
