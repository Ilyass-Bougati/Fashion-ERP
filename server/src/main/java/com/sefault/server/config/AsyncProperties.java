package com.sefault.server.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "app.async")
public record AsyncProperties(
        @DefaultValue("2") int corePoolSize,
        @DefaultValue("10") int maxPoolSize,
        @DefaultValue("500") int queueCapacity,
        @DefaultValue("EmailThread-") String threadNamePrefix) {}
