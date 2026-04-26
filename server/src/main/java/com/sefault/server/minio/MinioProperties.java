package com.sefault.server.minio;


import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "minio")
public record MinioProperties(
        @DefaultValue("http://localhost:9000") String endpoint,
        String accessKey,
        String secretKey
) {
}
