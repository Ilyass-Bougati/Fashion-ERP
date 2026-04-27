package com.sefault.server.properties;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "jwt")
public record JwtProperties(
        RSAPublicKey accessTokenPublicKey,
        RSAPrivateKey accessTokenPrivateKey,
        Duration accessTokenExpirationDuration,
        Duration refreshTokenExpirationDuration) {}
