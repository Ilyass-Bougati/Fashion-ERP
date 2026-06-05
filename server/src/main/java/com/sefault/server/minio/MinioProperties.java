package com.sefault.server.minio;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "minio")
public record MinioProperties(
        @DefaultValue("http://localhost:9000") String endpoint,
        // Public-facing URL used when generating pre-signed download URLs.
        // Must differ from endpoint when MinIO is behind a private hostname (e.g. inside Docker).
        // Falls back to endpoint when not set.
        String publicEndpoint,
        @DefaultValue("images") String imagesBucket,
        @DefaultValue("reports") String reportsBucket,
        @DefaultValue("60") int expirationDuration,
        String accessKey,
        String secretKey) {

    public String effectivePublicEndpoint() {
        return (publicEndpoint != null && !publicEndpoint.isBlank()) ? publicEndpoint : endpoint;
    }
}
