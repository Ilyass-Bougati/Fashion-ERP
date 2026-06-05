package com.sefault.server.minio;

import io.minio.MinioClient;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(MinioProperties.class)
public class MinioConfig {
    private final MinioProperties minioProperties;

    @Bean
    @Primary
    public MinioClient minioClient() {
        return MinioClient.builder()
                .endpoint(minioProperties.endpoint())
                .credentials(minioProperties.accessKey(), minioProperties.secretKey())
                .build();
    }

    // Separate client used only for generating pre-signed URLs.
    // Pre-signed URL generation is a local computation — no network call is made —
    // so this client never needs to reach the public host directly.
    // Its endpoint is what gets embedded in the signed URL's Host header,
    // which must match what the browser/client actually connects to.
    @Bean
    @Qualifier("presignClient")
    public MinioClient presignMinioClient() {
        return MinioClient.builder()
                .endpoint(minioProperties.effectivePublicEndpoint())
                .credentials(minioProperties.accessKey(), minioProperties.secretKey())
                // Region must be set explicitly so the SDK skips its region-discovery
                // HTTP call. Without this, it tries to reach the public endpoint from
                // inside Docker and times out. MinIO defaults to us-east-1.
                .region("us-east-1")
                .build();
    }
}
