package com.sefault.server.security.service;

import java.time.Duration;
import org.springframework.security.core.Authentication;

public interface TokenService {
    String generateToken(Authentication authentication, Duration expiration);
}
