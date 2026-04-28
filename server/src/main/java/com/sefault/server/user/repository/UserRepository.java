package com.sefault.server.user.repository;

import com.sefault.server.user.dto.projection.UserProjection;
import com.sefault.server.user.entity.User;
import io.lettuce.core.dynamic.annotation.Param;
import java.util.Optional;
import java.util.UUID;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<@NonNull User, @NonNull UUID> {
    Optional<UserProjection> getUserProjectionById(UUID id);

    @Query("""
            SELECT u FROM User u
            LEFT JOIN FETCH u.userAuthorities ua
            LEFT JOIN FETCH ua.authority
            WHERE u.email = :email
    """)
    Optional<User> findUserByEmailWithAuthorities(@Param("email") String email);

    User findByEmail(String mail);

    boolean existsByEmail(String email);
}
