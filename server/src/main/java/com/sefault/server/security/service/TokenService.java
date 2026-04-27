package com.sefault.server.security.service;

import org.springframework.security.core.Authentication;

import java.time.Duration;

public interface TokenService {
    String generateToken(Authentication authentication, Duration expiration);
}
