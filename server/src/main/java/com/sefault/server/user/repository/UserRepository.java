package com.sefault.server.user.repository;

import com.sefault.server.user.dto.projection.UserProjection;
import com.sefault.server.user.entity.User;
import java.util.Optional;
import java.util.UUID;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<@NonNull User, @NonNull UUID> {
    Optional<UserProjection> getUserProjectionById(UUID id);
}
