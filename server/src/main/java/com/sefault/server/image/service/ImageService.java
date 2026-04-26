package com.sefault.server.image.service;

import com.sefault.server.image.dto.record.ImageRecord;
import com.sefault.server.image.dto.record.ImageUrlRecord;
import io.minio.errors.MinioException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

public interface ImageService {
    ImageUrlRecord uploadImage(MultipartFile file) throws IOException, MinioException;
    ImageRecord findImageById(UUID id);
    void deleteImageById(UUID id) throws MinioException;
    String getImageUrl(UUID id);
}
