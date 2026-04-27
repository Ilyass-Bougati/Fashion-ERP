package com.sefault.server.security.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sefault.server.security.CustomUserDetailsService;
import com.sefault.server.security.config.SecurityConfig;
import com.sefault.server.security.dto.LoginRequest;
import com.sefault.server.security.filter.JwtCookieFilter;
import com.sefault.server.security.properties.JwtProperties;
import com.sefault.server.security.service.TokenService;
import com.sefault.server.security.util.CookieUtil;
import jakarta.servlet.http.Cookie;
import java.time.Duration;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseCookie;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@Import({SecurityConfig.class, JwtCookieFilter.class, JacksonAutoConfiguration.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    // --- MOCKING THE ENTIRE SECURITY ENGINE ---
    @MockitoBean
    private AuthenticationManager authenticationManager;

    @MockitoBean
    private TokenService tokenService;

    @MockitoBean
    private CookieUtil cookieUtil;

    @MockitoBean
    private JwtProperties jwtProperties;

    @MockitoBean
    private CustomUserDetailsService userDetailsService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    // ==========================================
    // 1. LOGIN TESTS
    // ==========================================

    @Test
    void login_Success_ReturnsCookies() throws Exception {
        LoginRequest request = new LoginRequest("admin@sefault.com", "password");

        Authentication mockAuth = new UsernamePasswordAuthenticationToken(request.email(), request.password());
        when(authenticationManager.authenticate(any())).thenReturn(mockAuth);

        when(tokenService.generateToken(any(), eq(Duration.ofMinutes(15)))).thenReturn("mock-access-token");
        when(tokenService.generateToken(any(), eq(Duration.ofDays(7)))).thenReturn("mock-refresh-token");

        when(jwtProperties.accessTokenExpirationDuration()).thenReturn(Duration.ofMinutes(15));
        when(jwtProperties.refreshTokenExpirationDuration()).thenReturn(Duration.ofDays(7));

        when(cookieUtil.createAccessTokenCookie(any(), anyLong()))
                .thenReturn(
                        ResponseCookie.from("access_token", "mock-access-token").build());
        when(cookieUtil.createRefreshTokenCookie(any(), anyLong()))
                .thenReturn(ResponseCookie.from("refresh_token", "mock-refresh-token")
                        .build());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(cookie().value("access_token", "mock-access-token"))
                .andExpect(cookie().value("refresh_token", "mock-refresh-token"))
                .andExpect(content().string("Login successful"));
    }

    @Test
    void login_InvalidCredentials_Returns401() throws Exception {
        LoginRequest request = new LoginRequest("admin@sefault.com", "wrong-password");

        when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Invalid email or password"));
    }

    // ==========================================
    // 2. REFRESH TESTS
    // ==========================================

    @Test
    void refresh_Success_ReturnsNewCookies() throws Exception {
        Jwt mockJwt = mock(Jwt.class);
        when(mockJwt.getSubject()).thenReturn("admin@sefault.com");
        when(jwtDecoder.decode("valid-refresh-token")).thenReturn(mockJwt);

        UserDetails mockUser =
                new User("admin@sefault.com", "password", true, true, true, true, Collections.emptyList());
        when(userDetailsService.loadUserByUsername("admin@sefault.com")).thenReturn(mockUser);

        when(tokenService.generateToken(any(), eq(Duration.ofMinutes(15)))).thenReturn("mock-access-token");
        when(tokenService.generateToken(any(), eq(Duration.ofDays(7)))).thenReturn("mock-refresh-token");
        when(jwtProperties.accessTokenExpirationDuration()).thenReturn(Duration.ofMinutes(15));
        when(jwtProperties.refreshTokenExpirationDuration()).thenReturn(Duration.ofDays(7));
        when(cookieUtil.createAccessTokenCookie(any(), anyLong()))
                .thenReturn(
                        ResponseCookie.from("access_token", "new-access-token").build());
        when(cookieUtil.createRefreshTokenCookie(any(), anyLong()))
                .thenReturn(ResponseCookie.from("refresh_token", "new-refresh-token")
                        .build());

        mockMvc.perform(post("/api/v1/auth/refresh").cookie(new Cookie("refresh_token", "valid-refresh-token")))
                .andExpect(status().isOk())
                .andExpect(cookie().value("access_token", "new-access-token"))
                .andExpect(cookie().value("refresh_token", "new-refresh-token"))
                .andExpect(content().string("Tokens refreshed successfully"));
    }

    @Test
    void refresh_MissingCookie_Returns401() throws Exception {
        mockMvc.perform(post("/api/v1/auth/refresh"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Refresh token is missing"));
    }

    @Test
    void refresh_InvalidOrExpiredToken_Returns401AndClearsCookies() throws Exception {
        when(jwtDecoder.decode(anyString())).thenThrow(new BadJwtException("Expired token"));

        when(cookieUtil.clearCookie("access_token"))
                .thenReturn(ResponseCookie.from("access_token", "").maxAge(0).build());
        when(cookieUtil.clearCookie("refresh_token"))
                .thenReturn(ResponseCookie.from("refresh_token", "").maxAge(0).build());

        mockMvc.perform(post("/api/v1/auth/refresh").cookie(new Cookie("refresh_token", "expired-refresh-token")))
                .andExpect(status().isUnauthorized())
                .andExpect(cookie().maxAge("access_token", 0))
                .andExpect(cookie().maxAge("refresh_token", 0))
                .andExpect(content().string("Refresh token expired or invalid. Please log in again."));
    }

    @Test
    void refresh_UserDisabled_Returns403() throws Exception {
        Jwt mockJwt = mock(Jwt.class);
        when(mockJwt.getSubject()).thenReturn("banned@sefault.com");
        when(jwtDecoder.decode("valid-refresh-token")).thenReturn(mockJwt);

        UserDetails disabledUser =
                new User("banned@sefault.com", "password", false, true, true, true, Collections.emptyList());
        when(userDetailsService.loadUserByUsername("banned@sefault.com")).thenReturn(disabledUser);

        mockMvc.perform(post("/api/v1/auth/refresh").cookie(new Cookie("refresh_token", "valid-refresh-token")))
                .andExpect(status().isForbidden())
                .andExpect(content().string("User account is disabled"));
    }

    // ==========================================
    // 3. LOGOUT & REGISTER TESTS
    // ==========================================

    @Test
    void logout_Success_ClearsCookies() throws Exception {
        when(cookieUtil.clearCookie("access_token"))
                .thenReturn(ResponseCookie.from("access_token", "").maxAge(0).build());
        when(cookieUtil.clearCookie("refresh_token"))
                .thenReturn(ResponseCookie.from("refresh_token", "").maxAge(0).build());

        mockMvc.perform(post("/api/v1/auth/logout"))
                .andExpect(status().isOk())
                .andExpect(cookie().maxAge("access_token", 0))
                .andExpect(cookie().maxAge("refresh_token", 0))
                .andExpect(content().string("Logged out successfully"));
    }
}
