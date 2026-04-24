package com.sefault.server.cache;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Configuration properties for Redis connection and caching.
 * @param timeToLive The duration in seconds before cache entries expire. The default value is 30 minutes.
 * Must be greater than 0.
 */
@ConfigurationProperties(prefix = "redis")
public record RedisProperties(@DefaultValue("1800000") Long timeToLive) {}
