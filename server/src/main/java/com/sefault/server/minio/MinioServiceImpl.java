package com.sefault.server.minio;

import io.minio.*;
import io.minio.errors.MinioException;
import java.io.IOException;
import java.io.InputStream;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class MinioServiceImpl implements MinioService {
    private final MinioClient minioClient;
    private final MinioProperties minioProperties;

    // TODO : refactor this later...
    @Setter
    private String bucketName = "default";

    public void uploadFile(String objectName, MultipartFile file) throws MinioException, IOException {
        uploadFile(objectName, file.getInputStream(), file.getSize(), file.getContentType());
    }

    // I added this so that I could save reports, since they are generated not uploaded as MultipartFiles
    public void uploadFile(String objectName, InputStream inputStream, long size, String contentType)
            throws MinioException, IOException {
        boolean found = minioClient.bucketExists(
                BucketExistsArgs.builder().bucket(bucketName).build());

        if (!found) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        }

        try (inputStream) {
            minioClient.putObject(
                    PutObjectArgs.builder().bucket(bucketName).object(objectName).stream(inputStream, size, -1L)
                            .contentType(contentType)
                            .build());
        }
    }

    public String getFileUrl(String objectName, int expiry) throws MinioException {
        return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                .method(Http.Method.GET)
                .bucket(bucketName)
                .object(objectName)
                .expiry(expiry)
                .build());
    }

    public void deleteFile(String objectName) throws MinioException {
        minioClient.removeObject(
                RemoveObjectArgs.builder().bucket(bucketName).object(objectName).build());
    }

    public String getPermanentFileUrl(String objectName) {
        return minioProperties.endpoint() + "/" + bucketName + "/" + objectName;
    }
}
