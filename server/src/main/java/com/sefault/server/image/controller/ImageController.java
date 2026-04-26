package com.sefault.server.image.controller;

import com.sefault.server.image.dto.record.ImageRecord;
import com.sefault.server.image.dto.record.ImageUrlRecord;
import com.sefault.server.image.service.ImageServiceImpl;
import io.minio.errors.MinioException;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/images")
@RequiredArgsConstructor
public class ImageController {
    private final ImageServiceImpl imageService;

    @PostMapping("/upload")
    public ResponseEntity<ImageUrlRecord> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            ImageUrlRecord image = imageService.uploadImage(file);
            return ResponseEntity.ok(image);
        } catch (Exception e) {
            throw new RuntimeException("Upload failed: " + e.getMessage());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<ImageRecord> getImage(@PathVariable UUID id) {
        ImageRecord image = imageService.findImageById(id);
        return ResponseEntity.ok(image);
    }

    @GetMapping("/{id}/url")
    public ResponseEntity<Map<String, String>> getImageUrl(@PathVariable UUID id) {
        String url = imageService.getImageUrl(id);
        return ResponseEntity.ok(Map.of("url", url));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteImage(@PathVariable UUID id) throws MinioException {
        imageService.deleteImageById(id);
    }
}
