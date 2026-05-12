package com.sefault.server.sales.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sefault.server.exception.GlobalExceptionHandler;
import com.sefault.server.sales.dto.record.SaleLineRecord;
import com.sefault.server.sales.entity.id.SaleLineId;
import com.sefault.server.sales.service.SaleLineService;
import com.sefault.server.security.CustomUserDetailsService;
import com.sefault.server.security.config.SecurityConfig;
import com.sefault.server.security.filter.JwtCookieFilter;
import com.sefault.server.security.properties.ApplicationAuthorities;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(SaleLineController.class)
@org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc(addFilters = false)
@Import({SecurityConfig.class, JwtCookieFilter.class, JacksonAutoConfiguration.class, GlobalExceptionHandler.class})
public class SaleLineControllerTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @MockitoBean
    private SaleLineService saleLineService;

    @MockitoBean(name = "authorities")
    private ApplicationAuthorities authorities;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @BeforeEach
    void setUp() {
        when(authorities.getCreateSaleLineAuthority()).thenReturn("CREATE_SALE_LINE");
        when(authorities.getReadSaleLineAuthority()).thenReturn("READ_SALE_LINE");
        when(authorities.getDeleteSaleLineAuthority()).thenReturn("DELETE_SALE_LINE");
    }

    @Test
    @WithMockUser(authorities = "CREATE_SALE_LINE")
    void createSaleLine_Success() throws Exception {
        UUID saleId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        SaleLineId id = new SaleLineId(saleId, productId);
        SaleLineRecord record = new SaleLineRecord(id, 2, saleId, productId, 99.99);

        when(saleLineService.create(any())).thenReturn(record);

        mockMvc.perform(post("/api/v1/sale-line")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(record)))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "DELETE_SALE_LINE")
    void deleteSaleLine_Success() throws Exception {
        UUID saleId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/sale-line/{saleId}/{productId}", saleId, productId))
                .andExpect(status().isOk());
    }
}
