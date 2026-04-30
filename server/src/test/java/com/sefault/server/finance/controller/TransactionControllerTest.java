package com.sefault.server.finance.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sefault.server.exception.NotFoundException;
import com.sefault.server.finance.dto.record.TransactionRecord;
import com.sefault.server.finance.enums.TransactionType;
import com.sefault.server.finance.service.TransactionService;
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

@WebMvcTest(TransactionController.class)
@org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc(addFilters = false)
@Import({
        SecurityConfig.class,
        JwtCookieFilter.class,
        JacksonAutoConfiguration.class,
        TransactionControllerTest.TestAdvice.class
})
public class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @MockitoBean
    private TransactionService transactionService;

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
        when(authorities.getCreateTransactionAuthority()).thenReturn("CREATE_TRANSACTION");
        when(authorities.getReadTransactionAuthority()).thenReturn("READ_TRANSACTION");
        when(authorities.getReverseTransactionAuthority()).thenReturn("REVERSE_TRANSACTION");
    }

    @Test
    @WithMockUser(authorities = "CREATE_TRANSACTION")
    void createTransaction_Success() throws Exception {
        UUID id = UUID.randomUUID();
        TransactionRecord record = new TransactionRecord(id, TransactionType.PAID, null, 100.0, LocalDateTime.now());
        when(transactionService.createTransaction(any())).thenReturn(record);

        mockMvc.perform(post("/api/v1/finance/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(record)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id.toString()));
    }

    @Test
    @WithMockUser(authorities = "CREATE_TRANSACTION")
    void createTransaction_400_Malformed() throws Exception {
        mockMvc.perform(post("/api/v1/finance/transactions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\": \"invalid\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "READ_TRANSACTION")
    void getTransaction_404() throws Exception {
        UUID id = UUID.randomUUID();
        // Updated to throw your custom exception!
        when(transactionService.getTransaction(id)).thenThrow(new NotFoundException("Not found"));

        mockMvc.perform(get("/api/v1/finance/transactions/{id}", id)).andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "REVERSE_TRANSACTION")
    void reverseTransaction_Success() throws Exception {
        UUID id = UUID.randomUUID();
        when(transactionService.reverseTransaction(id))
                .thenReturn(new TransactionRecord(id, TransactionType.RECEIVED, null, 100.0, LocalDateTime.now()));
        mockMvc.perform(post("/api/v1/finance/transactions/{id}/reverse", id)).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "READ_TRANSACTION")
    void getAllTransactions_Success() throws Exception {
        when(transactionService.getAllTransactions(any(), any())).thenReturn(new PageImpl<>(List.of()));
        mockMvc.perform(get("/api/v1/finance/transactions")).andExpect(status().isOk());
    }

    @Test
    void unauthenticated_401() throws Exception {
        mockMvc.perform(get("/api/v1/finance/transactions")).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "WRONG_AUTHORITY")
    void unauthorized_403() throws Exception {
        mockMvc.perform(get("/api/v1/finance/transactions")).andExpect(status().isForbidden());
    }
}