package com.sefault.server.security.controller;

import com.sefault.server.rateLimiting.RateLimit;
import com.sefault.server.security.CustomUserDetailsService;
import com.sefault.server.security.dto.LoginRequest;
import com.sefault.server.security.properties.JwtProperties;
import com.sefault.server.security.service.TokenService;
import com.sefault.server.security.util.CookieUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@RateLimit(actionName = "auth_api")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final CookieUtil cookieUtil;
    private final JwtProperties jwtProperties;
    private final CustomUserDetailsService userDetailsService;
    private final JwtDecoder jwtDecoder;

    @PostMapping("/login")
    public ResponseEntity<String> login(@Valid @RequestBody LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password()));

        String accessToken = tokenService.generateToken(authentication, jwtProperties.accessTokenExpirationDuration());
        String refreshToken =
                tokenService.generateToken(authentication, jwtProperties.refreshTokenExpirationDuration());

        ResponseCookie accessCookie = cookieUtil.createAccessTokenCookie(
                accessToken, jwtProperties.accessTokenExpirationDuration().toMillis());
        ResponseCookie refreshCookie = cookieUtil.createRefreshTokenCookie(
                refreshToken, jwtProperties.refreshTokenExpirationDuration().toMillis());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body("Login successful");
    }

    @PostMapping("/logout")
    public ResponseEntity<String> logout() {
        ResponseCookie accessCookie = cookieUtil.clearCookie("access_token");
        ResponseCookie refreshCookie = cookieUtil.clearCookie("refresh_token");

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                .body("Logged out successfully");
    }

    @PostMapping("/refresh")
    public ResponseEntity<String> refreshTokens(
            @CookieValue(name = "refresh_token", required = false) String refreshToken) {

        if (refreshToken == null || refreshToken.isBlank()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh token is missing");
        }

        try {
            Jwt jwt = jwtDecoder.decode(refreshToken);
            String email = jwt.getSubject();

            UserDetails userDetails = userDetailsService.loadUserByUsername(email);

            if (!userDetails.isEnabled()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User account is disabled");
            }

            Authentication authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

            String newAccessToken =
                    tokenService.generateToken(authentication, jwtProperties.accessTokenExpirationDuration());
            String newRefreshToken =
                    tokenService.generateToken(authentication, jwtProperties.refreshTokenExpirationDuration());

            ResponseCookie accessCookie = cookieUtil.createAccessTokenCookie(
                    newAccessToken,
                    jwtProperties.accessTokenExpirationDuration().toMillis());
            ResponseCookie refreshCookie = cookieUtil.createRefreshTokenCookie(
                    newRefreshToken,
                    jwtProperties.refreshTokenExpirationDuration().toMillis());

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                    .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                    .body("Tokens refreshed successfully");

        } catch (JwtException e) {
            ResponseCookie accessCookie = cookieUtil.clearCookie("access_token");
            ResponseCookie refreshCookie = cookieUtil.clearCookie("refresh_token");

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .header(HttpHeaders.SET_COOKIE, accessCookie.toString())
                    .header(HttpHeaders.SET_COOKIE, refreshCookie.toString())
                    .body("Refresh token expired or invalid. Please log in again.");
        }
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<String> handleBadCredentials(BadCredentialsException ex) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid email or password");
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<String> handleDisabledAccount(DisabledException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body("User account is disabled");
    }
}
