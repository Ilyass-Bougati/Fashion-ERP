package com.sefault.server.finance.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sefault.server.exception.NotFoundException;
import com.sefault.server.finance.dto.record.FixChargeRecord;
import com.sefault.server.finance.service.FixChargeService;
import com.sefault.server.security.CustomUserDetailsService;
import com.sefault.server.security.config.SecurityConfig;
import com.sefault.server.security.filter.JwtCookieFilter;
import com.sefault.server.security.properties.ApplicationAuthorities;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@WebMvcTest(FixChargeController.class)
@org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc(addFilters = false)
@Import({
        SecurityConfig.class,
        JwtCookieFilter.class,
        JacksonAutoConfiguration.class,
        FixChargeControllerTest.TestAdvice.class
})
public class FixChargeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @MockitoBean
    private FixChargeService fixChargeService;

    @MockitoBean(name = "authorities")
    private ApplicationAuthorities authorities;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @RestControllerAdvice
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public static class TestAdvice {
        @ExceptionHandler(NotFoundException.class)
        @ResponseStatus(HttpStatus.NOT_FOUND)
        public void handleNotFound() {}

        @ExceptionHandler(org.springframework.security.authentication.AuthenticationCredentialsNotFoundException.class)
        @ResponseStatus(HttpStatus.UNAUTHORIZED)
        public void handle401() {}

        @ExceptionHandler(org.springframework.security.authorization.AuthorizationDeniedException.class)
        @ResponseStatus(HttpStatus.FORBIDDEN)
        public void handle403() {}

        @ExceptionHandler(Exception.class)
        @ResponseStatus(HttpStatus.BAD_REQUEST)
        public void handleAll() {}
    }

    @BeforeEach
    void setUp() {
        when(authorities.getCreateFixedChargeAuthority()).thenReturn("CREATE_FIXED_CHARGE");
        when(authorities.getReadFixedChargeAuthority()).thenReturn("READ_FIXED_CHARGE");
        when(authorities.getUpdateFixedChargeAuthority()).thenReturn("UPDATE_FIXED_CHARGE");
        when(authorities.getToggleFixedChargeAuthority()).thenReturn("TOGGLE_FIXED_CHARGE");
    }

    @Test
    @WithMockUser(authorities = "CREATE_FIXED_CHARGE")
    void createFixedCharge_Success() throws Exception {
        FixChargeRecord record =
                new FixChargeRecord(UUID.randomUUID(), "Rent", "Office", 500.0, true, LocalDateTime.now());
        when(fixChargeService.createFixCharge(any())).thenReturn(record);
        mockMvc.perform(post("/api/v1/finance/fixed-charges")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(record)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "TOGGLE_FIXED_CHARGE")
    void toggleFixedCharge_Success() throws Exception {
        mockMvc.perform(patch("/api/v1/finance/fixed-charges/{id}/toggle", UUID.randomUUID()))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(authorities = "READ_FIXED_CHARGE")
    void getFixCharge_Success() throws Exception {
        UUID id = UUID.randomUUID();
        FixChargeRecord record = new FixChargeRecord(id, "Rent", "Office", 500.0, true, LocalDateTime.now());
        when(fixChargeService.getFixCharge(id)).thenReturn(record);
        mockMvc.perform(get("/api/v1/finance/fixed-charges/{id}", id)).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "UPDATE_FIXED_CHARGE")
    void updateFixedCharge_Success() throws Exception {
        FixChargeRecord record =
                new FixChargeRecord(UUID.randomUUID(), "Rent", "Office", 600.0, true, LocalDateTime.now());
        when(fixChargeService.updateFixCharge(any())).thenReturn(record);
        mockMvc.perform(put("/api/v1/finance/fixed-charges")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(record)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "READ_FIXED_CHARGE")
    void getAllFixCharges_Success() throws Exception {
        when(fixChargeService.getAllFixCharges(any(Boolean.class), any())).thenReturn(new PageImpl<>(List.of()));
        mockMvc.perform(get("/api/v1/finance/fixed-charges")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "WRONG_AUTHORITY")
    void unauthorized_403() throws Exception {
        mockMvc.perform(get("/api/v1/finance/fixed-charges")).andExpect(status().isForbidden());
    }
}