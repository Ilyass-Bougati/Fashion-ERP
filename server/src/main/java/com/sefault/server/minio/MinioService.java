package com.sefault.server.minio;

import io.minio.errors.MinioException;
import java.io.IOException;
import org.springframework.web.multipart.MultipartFile;

public interface MinioService {
    void setBucketName(String bucketName);
  
    String getFileUrl(String objectName, int expiry) throws MinioException;

    void uploadFile(String objectName, MultipartFile file) throws MinioException, IOException;

    void deleteFile(String objectName) throws MinioException;

    String getPermanentFileUrl(String objectName);
}
