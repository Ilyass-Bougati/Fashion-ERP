package com.sefault.server.sales.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sefault.server.exception.NotFoundException;
import com.sefault.server.sales.dto.record.SaleRecord;
import com.sefault.server.sales.service.SaleService;
import com.sefault.server.security.CustomUserDetailsService;
import com.sefault.server.security.config.SecurityConfig;
import com.sefault.server.security.filter.JwtCookieFilter;
import com.sefault.server.security.properties.ApplicationAuthorities;
import java.time.LocalDateTime;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@WebMvcTest(SaleController.class)
@org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc(addFilters = false)
@Import({
    SecurityConfig.class,
    JwtCookieFilter.class,
    JacksonAutoConfiguration.class,
    SaleControllerTest.TestAdvice.class // <-- Changement ici (on utilise le TestAdvice)
})
public class SaleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @MockitoBean
    private SaleService saleService;

    @MockitoBean(name = "authorities")
    private ApplicationAuthorities authorities;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    // Ajout du TestAdvice, exactement comme dans PayrollControllerTest
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
        when(authorities.getCreateSaleAuthority()).thenReturn("CREATE_SALE");
        when(authorities.getReadSaleAuthority()).thenReturn("READ_SALE");
        when(authorities.getUpdateSaleAuthority()).thenReturn("UPDATE_SALE");
        when(authorities.getDeleteSaleAuthority()).thenReturn("DELETE_SALE");
    }

    @Test
    @WithMockUser(authorities = "CREATE_SALE")
    void createSale_Success() throws Exception {
        UUID id = UUID.randomUUID();
        SaleRecord record = new SaleRecord(id, 0.0, UUID.randomUUID(), false, LocalDateTime.now(), LocalDateTime.now());
        when(saleService.create(any())).thenReturn(record);

        mockMvc.perform(post("/api/v1/sale")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(record)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    @WithMockUser(authorities = "READ_SALE")
    void getSaleById_404() throws Exception {
        UUID id = UUID.randomUUID();
        when(saleService.getById(id)).thenThrow(new NotFoundException("Sale not found"));

        mockMvc.perform(get("/api/v1/sale/{id}", id)).andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "WRONG_AUTHORITY")
    void unauthorized_Returns403() throws Exception {
        mockMvc.perform(get("/api/v1/sale"))
                .andExpect(status().isForbidden()); // Maintenant, ça retournera bien un 403 !
    }
}
