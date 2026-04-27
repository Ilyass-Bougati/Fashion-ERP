package com.sefault.server.rateLimiting;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sefault.server.exception.RateLimitExceededException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import({RateLimitIntegrationTest.DummyRateLimitController.class, RateLimitIntegrationTest.TestExceptionHandler.class})
@AutoConfigureMockMvc(addFilters = false)
class RateLimitIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @RestController
    static class DummyRateLimitController {
        @GetMapping("/api/test/ip-limit")
        @RateLimit(actionName = "testIp", capacity = 2, tokensPerMinute = 2)
        public String testIpLimiting() {
            return "Success";
        }

        @GetMapping("/api/test/spel-limit")
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
    void shouldAllowRequestsUnderLimitAndBlockWhenExceeded() throws Exception {
        mockMvc.perform(get("/api/test/ip-limit").header("X-Forwarded-For", "192.168.1.1"))
                .andExpect(status().isOk())
                .andExpect(header().exists("X-RateLimit-Limit"))
                .andExpect(header().exists("X-RateLimit-Remaining"));

        mockMvc.perform(get("/api/test/ip-limit").header("X-Forwarded-For", "192.168.1.1"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/test/ip-limit").header("X-Forwarded-For", "192.168.1.1"))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().exists("Retry-After"));
    }

    @Test
    void shouldTrackDifferentIpsSeparately() throws Exception {

        mockMvc.perform(get("/api/test/ip-limit").header("X-Forwarded-For", "10.0.0.1"))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/test/ip-limit").header("X-Forwarded-For", "10.0.0.1"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/test/ip-limit").header("X-Forwarded-For", "10.0.0.1"))
                .andExpect(status().isTooManyRequests());

        mockMvc.perform(get("/api/test/ip-limit").header("X-Forwarded-For", "10.0.0.2"))
                .andExpect(status().isOk());
    }

    @Test
    void shouldEvaluateSpelExpressionCorrectly() throws Exception {
        mockMvc.perform(get("/api/test/spel-limit").param("userId", "userA")).andExpect(status().isOk());

        mockMvc.perform(get("/api/test/spel-limit").param("userId", "userA")).andExpect(status().isTooManyRequests());

        mockMvc.perform(get("/api/test/spel-limit").param("userId", "userB")).andExpect(status().isOk());
    }
}
