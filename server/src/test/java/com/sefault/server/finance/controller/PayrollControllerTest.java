package com.sefault.server.finance.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sefault.server.exception.NotFoundException;
import com.sefault.server.finance.dto.record.PayrollRecord;
import com.sefault.server.finance.service.PayrollService;
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
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@WebMvcTest(PayrollController.class)
@org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc(addFilters = false)
@Import({
        SecurityConfig.class,
        JwtCookieFilter.class,
        JacksonAutoConfiguration.class,
        PayrollControllerTest.TestAdvice.class
})
public class PayrollControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @MockitoBean
    private PayrollService payrollService;

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
        when(authorities.getProcessPayrollAuthority()).thenReturn("PROCESS_PAYROLL");
        when(authorities.getReadPayrollAuthority()).thenReturn("READ_PAYROLL");
    }

    @Test
    @WithMockUser(authorities = "PROCESS_PAYROLL")
    void processPayroll_Success() throws Exception {
        UUID empId = UUID.randomUUID();
        PayrollRecord record =
                new PayrollRecord(UUID.randomUUID(), 1000.0, UUID.randomUUID(), empId, 50.0, LocalDateTime.now());
        when(payrollService.processPayroll(eq(empId), any(), any())).thenReturn(record);

        mockMvc.perform(post("/api/v1/finance/payroll/process/{employeeId}", empId)
                        .param("startDate", "2023-01-01T00:00:00")
                        .param("endDate", "2023-01-31T23:59:59"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "PROCESS_PAYROLL")
    void processPayroll_400_MissingParams() throws Exception {
        mockMvc.perform(post("/api/v1/finance/payroll/process/{employeeId}", UUID.randomUUID()))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(authorities = "READ_PAYROLL")
    void getPayroll_Success() throws Exception {
        UUID id = UUID.randomUUID();
        when(payrollService.getPayroll(id))
                .thenReturn(
                        new PayrollRecord(id, 1000.0, UUID.randomUUID(), UUID.randomUUID(), 50.0, LocalDateTime.now()));
        mockMvc.perform(get("/api/v1/finance/payroll/{id}", id)).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "READ_PAYROLL")
    void getAllPayrolls_Success() throws Exception {
        when(payrollService.getAllPayrolls(any())).thenReturn(new PageImpl<>(List.of()));
        mockMvc.perform(get("/api/v1/finance/payroll")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "READ_PAYROLL")
    void getPayrollHistoryForEmployee_Success() throws Exception {
        when(payrollService.getPayrollHistoryForEmployee(any(), any())).thenReturn(new PageImpl<>(List.of()));
        mockMvc.perform(get("/api/v1/finance/payroll/employee/{employeeId}", UUID.randomUUID()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "WRONG_AUTHORITY")
    void unauthorized_403() throws Exception {
        mockMvc.perform(get("/api/v1/finance/payroll")).andExpect(status().isForbidden());
    }
}