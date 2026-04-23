package com.sefault.server.image.repository;

import com.sefault.server.image.dto.projection.ImageProjection;
import com.sefault.server.image.entity.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import lombok.NonNull;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ImageRepository extends JpaRepository<@NonNull Image, @NonNull UUID> {
    Optional<ImageProjection> getImageProjectionById(UUID id);
}