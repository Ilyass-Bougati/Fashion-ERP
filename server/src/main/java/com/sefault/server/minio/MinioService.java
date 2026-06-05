package com.sefault.server.minio;

import io.minio.errors.MinioException;
import java.io.IOException;
import java.io.InputStream;
import org.springframework.web.multipart.MultipartFile;

public interface MinioService {
    void setBucketName(String bucketName);

    String getFileUrl(String bucketName, String objectName, int expiry) throws MinioException;

    InputStream getObject(String bucketName, String objectName) throws MinioException, IOException;

    void uploadFile(String bucketName, String objectName, InputStream inputStream, long size, String contentType)
            throws MinioException, IOException;

    void uploadFile(String bucketName, String objectName, MultipartFile file) throws MinioException, IOException;

    void deleteFile(String bucketName, String objectName) throws MinioException;

    String getPermanentFileUrl(String bucketName, String objectName);
}
