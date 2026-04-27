package com.sefault.server.image.repository;

import com.sefault.server.image.dto.projection.ImageProjection;
import com.sefault.server.image.entity.Image;
import java.util.Optional;
import java.util.UUID;
import lombok.NonNull;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ImageRepository extends JpaRepository<@NonNull Image, @NonNull UUID> {
    Optional<ImageProjection> getImageProjectionById(UUID id);
}
