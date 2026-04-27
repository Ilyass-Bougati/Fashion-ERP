package com.sefault.server.rateLimiting;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.anonymous;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sefault.server.exception.RateLimitExceededException;
import com.sefault.server.security.config.JwtConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({RateLimitIntegrationTest.DummyRateLimitController.class, RateLimitIntegrationTest.TestExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
@WithMockUser
class RateLimitIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @RestController
    static class DummyRateLimitController {
        @GetMapping("/test/api/ip-limit")
        @RateLimit(actionName = "testIp", capacity = 2, tokensPerMinute = 2)
        public String testIpLimiting() {
            return "Success";
        }

        @GetMapping("/test/api/spel-limit")
        @RateLimit(actionName = "testSpel", capacity = 1, tokensPerMinute = 1, keyExpression = "#userId")
        public String testSpelLimiting(String userId) {
            return "Success";
        }
    }

    @RestControllerAdvice
    static class TestExceptionHandler {
        @ExceptionHandler(RateLimitExceededException.class)
        public ResponseEntity<String> handleRateLimitException(RateLimitExceededException ex) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body("Too many requests");
        }
    }

    @Test
    @WithMockUser(username = "test-user-1")
    void shouldAllowRequestsUnderLimitAndBlockWhenExceeded() throws Exception {
        mockMvc.perform(get("/test/api/ip-limit").header("X-Forwarded-For", "192.168.1.1"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-RateLimit-Limit"))
                .andExpect(header().exists("X-RateLimit-Remaining"));

        mockMvc.perform(get("/test/api/ip-limit").header("X-Forwarded-For", "192.168.1.1"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/test/api/ip-limit").header("X-Forwarded-For", "192.168.1.1"))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"));
    }

    @Test
    void shouldTrackDifferentIpsSeparately() throws Exception {
        SecurityContextHolder.clearContext();
        mockMvc.perform(get("/test/api/ip-limit").with(anonymous()).header("X-Forwarded-For", "10.0.0.1"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/test/api/ip-limit").with(anonymous()).header("X-Forwarded-For", "10.0.0.1"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/test/api/ip-limit").with(anonymous()).header("X-Forwarded-For", "10.0.0.1"))
                .andExpect(status().isTooManyRequests());

        mockMvc.perform(get("/test/api/ip-limit").with(anonymous()).header("X-Forwarded-For", "10.0.0.2"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "test-user-3")
    void shouldEvaluateSpelExpressionCorrectly() throws Exception {
        mockMvc.perform(get("/test/api/spel-limit").param("userId", "userA")).andExpect(status().isOk());

        mockMvc.perform(get("/test/api/spel-limit").param("userId", "userA")).andExpect(status().isTooManyRequests());

        mockMvc.perform(get("/test/api/spel-limit").param("userId", "userB")).andExpect(status().isOk());
    }
}
