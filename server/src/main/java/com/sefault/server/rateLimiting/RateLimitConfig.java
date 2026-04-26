package com.sefault.server.rateLimiting;

import com.github.benmanes.caffeine.cache.Caffeine;
import io.github.bucket4j.caffeine.CaffeineProxyManager;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RateLimitConfig {

    @Bean
    public ProxyManager<String> caffeineProxyManager() {
        Caffeine<Object, Object> builder = Caffeine.newBuilder().maximumSize(10_000);

        return new CaffeineProxyManager<>(builder, Duration.ofHours(1));
    }
}
