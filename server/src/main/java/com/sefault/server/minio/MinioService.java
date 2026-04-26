package com.sefault.server.minio;

import io.minio.errors.MinioException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface MinioService {
    String getFileUrl(String objectName, int expiry) throws MinioException;
    void uploadFile(String objectName, MultipartFile file) throws MinioException, IOException;
    void deleteFile(String objectName) throws MinioException;
    String getPermanentFileUrl(String objectName);
}
