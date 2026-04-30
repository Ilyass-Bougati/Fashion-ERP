package com.sefault.server.finance.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sefault.server.finance.dto.record.FixChargeRecord;
import com.sefault.server.finance.dto.record.PayrollRecord;
import com.sefault.server.finance.dto.record.TransactionRecord;
import com.sefault.server.finance.enums.TransactionType;
import com.sefault.server.finance.service.FinanceService;
import com.sefault.server.security.CustomUserDetailsService;
import com.sefault.server.security.config.SecurityConfig;
import com.sefault.server.security.filter.JwtCookieFilter;
import com.sefault.server.security.properties.ApplicationAuthorities;
import jakarta.persistence.EntityNotFoundException;
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

@WebMvcTest(FinanceController.class)
@org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc(addFilters = false)
@Import({
    SecurityConfig.class,
    JwtCookieFilter.class,
    JacksonAutoConfiguration.class,
    FinanceControllerTest.TestAdvice.class
})
public class FinanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @MockitoBean
    private FinanceService financeService;

    @MockitoBean(name = "authorities")
    private ApplicationAuthorities authorities;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @RestControllerAdvice
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public static class TestAdvice {
        @ExceptionHandler(EntityNotFoundException.class)
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
        when(authorities.getCreateFixedChargeAuthority()).thenReturn("CREATE_FIXED_CHARGE");
        when(authorities.getReadFixedChargeAuthority()).thenReturn("READ_FIXED_CHARGE");
        when(authorities.getUpdateFixedChargeAuthority()).thenReturn("UPDATE_FIXED_CHARGE");
        when(authorities.getToggleFixedChargeAuthority()).thenReturn("TOGGLE_FIXED_CHARGE");
        when(authorities.getProcessPayrollAuthority()).thenReturn("PROCESS_PAYROLL");
        when(authorities.getReadPayrollAuthority()).thenReturn("READ_PAYROLL");
    }

    // --- Transactions ---

    @Test
    @WithMockUser(authorities = "CREATE_TRANSACTION")
    void createTransaction_Success() throws Exception {
        UUID id = UUID.randomUUID();
        TransactionRecord record = new TransactionRecord(id, TransactionType.PAID, null, 100.0, LocalDateTime.now());
        when(financeService.createTransaction(any())).thenReturn(record);

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
        when(financeService.getTransaction(id)).thenThrow(new EntityNotFoundException("Not found"));
        mockMvc.perform(get("/api/v1/finance/transactions/{id}", id)).andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(authorities = "REVERSE_TRANSACTION")
    void reverseTransaction_Success() throws Exception {
        UUID id = UUID.randomUUID();
        when(financeService.reverseTransaction(id))
                .thenReturn(new TransactionRecord(id, TransactionType.RECEIVED, null, 100.0, LocalDateTime.now()));
        mockMvc.perform(post("/api/v1/finance/transactions/{id}/reverse", id)).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "READ_TRANSACTION")
    void getAllTransactions_Success() throws Exception {
        when(financeService.getAllTransactions(any())).thenReturn(new PageImpl<>(List.of()));
        mockMvc.perform(get("/api/v1/finance/transactions")).andExpect(status().isOk());
    }

    // --- Payroll ---

    @Test
    @WithMockUser(authorities = "PROCESS_PAYROLL")
    void processPayroll_Success() throws Exception {
        UUID empId = UUID.randomUUID();
        PayrollRecord record =
                new PayrollRecord(UUID.randomUUID(), 1000.0, UUID.randomUUID(), empId, 50.0, LocalDateTime.now());
        when(financeService.processPayroll(eq(empId), any(), any())).thenReturn(record);

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
        when(financeService.getPayroll(id))
                .thenReturn(
                        new PayrollRecord(id, 1000.0, UUID.randomUUID(), UUID.randomUUID(), 50.0, LocalDateTime.now()));
        mockMvc.perform(get("/api/v1/finance/payroll/{id}", id)).andExpect(status().isOk());
    }

    // --- Fixed Charges ---

    @Test
    @WithMockUser(authorities = "CREATE_FIXED_CHARGE")
    void createFixedCharge_Success() throws Exception {
        FixChargeRecord record =
                new FixChargeRecord(UUID.randomUUID(), "Rent", "Office", 500.0, true, LocalDateTime.now());
        when(financeService.createFixCharge(any())).thenReturn(record);
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
    @WithMockUser(authorities = "READ_PAYROLL")
    void getAllPayrolls_Success() throws Exception {
        when(financeService.getAllPayrolls(any())).thenReturn(new PageImpl<>(List.of()));
        mockMvc.perform(get("/api/v1/finance/payroll")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "READ_PAYROLL")
    void getPayrollHistoryForEmployee_Success() throws Exception {
        when(financeService.getPayrollHistoryForEmployee(any(), any())).thenReturn(new PageImpl<>(List.of()));
        mockMvc.perform(get("/api/v1/finance/payroll/employee/{employeeId}", UUID.randomUUID()))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "READ_FIXED_CHARGE")
    void getFixCharge_Success() throws Exception {
        UUID id = UUID.randomUUID();
        FixChargeRecord record = new FixChargeRecord(id, "Rent", "Office", 500.0, true, LocalDateTime.now());
        when(financeService.getFixCharge(id)).thenReturn(record);
        mockMvc.perform(get("/api/v1/finance/fixed-charges/{id}", id)).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "UPDATE_FIXED_CHARGE")
    void updateFixedCharge_Success() throws Exception {
        FixChargeRecord record =
                new FixChargeRecord(UUID.randomUUID(), "Rent", "Office", 600.0, true, LocalDateTime.now());
        when(financeService.updateFixCharge(any())).thenReturn(record);
        mockMvc.perform(put("/api/v1/finance/fixed-charges")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(record)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "READ_FIXED_CHARGE")
    void getAllFixCharges_Success() throws Exception {
        when(financeService.getAllFixCharges(any())).thenReturn(new PageImpl<>(List.of()));
        mockMvc.perform(get("/api/v1/finance/fixed-charges")).andExpect(status().isOk());
    }

    // --- Security ---

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
