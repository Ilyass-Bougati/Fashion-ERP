package com.sefault.server.user.repository;

import com.sefault.server.user.entity.Authority;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AuthorityRepository extends JpaRepository<Authority, UUID> {
    <T> T findAuthorityById(UUID id, Class<T> type);

    Authority findByName(String name);
}