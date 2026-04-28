package com.sefault.server.security.service;

import org.springframework.security.core.Authentication;

public interface TokenService {
    String generateToken(Authentication authentication);

    String generateRefreshToken(Authentication authentication);
}
