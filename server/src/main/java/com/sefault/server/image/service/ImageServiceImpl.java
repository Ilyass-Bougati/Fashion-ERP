package com.sefault.server.image.service;

import com.sefault.server.exception.NotFoundException;
import com.sefault.server.image.dto.record.ImageRecord;
import com.sefault.server.image.dto.record.ImageUrlRecord;
import com.sefault.server.image.entity.Image;
import com.sefault.server.image.mapper.ImageMapper;
import com.sefault.server.image.repository.ImageRepository;
import com.sefault.server.minio.MinioProperties;
import com.sefault.server.minio.MinioService;
import io.minio.errors.MinioException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

@Service
@Transactional
public class ImageServiceImpl implements ImageService{
    private final MinioService minioService;
    private final ImageRepository imageRepository;
    private final MinioProperties minioProperties;
    private final ImageMapper imageMapper;

    public ImageServiceImpl(MinioService minioService, ImageRepository imageRepository, MinioProperties minioProperties, ImageMapper imageMapper) {
        this.minioService = minioService;
        this.imageRepository = imageRepository;
        this.minioProperties = minioProperties;
        this.imageMapper = imageMapper;
        this.minioService.setBucketName(minioProperties.imagesBucket());
    }

    public ImageUrlRecord uploadImage(MultipartFile file) throws IOException, MinioException {
        String objectName = UUID.randomUUID().toString();
        Image image = Image.builder()
                .bucketName(minioProperties.imagesBucket())
                .contentType(file.getContentType())
                .objectKey(objectName)
                .build();

        minioService.uploadFile(objectName, file);
        Image savedImage = imageRepository.save(image);

        return new ImageUrlRecord(savedImage.getId(), minioService.getPermanentFileUrl(objectName));
    }

    @Transactional(readOnly = true)
    public ImageRecord findImageById(UUID id) {
        return imageRepository.getImageProjectionById(id)
                .map(imageMapper::projectionToRecord)
                .orElseThrow(() -> new NotFoundException("Image not found with id: " + id));
    }

    public void deleteImageById(UUID id) throws MinioException {
        ImageRecord record = findImageById(id);
        minioService.deleteFile(record.objectKey());
        imageRepository.deleteById(id);
    }

    public String getImageUrl(UUID id) {
        ImageRecord record = findImageById(id);
        return minioService.getPermanentFileUrl(record.objectKey());
    }
}
