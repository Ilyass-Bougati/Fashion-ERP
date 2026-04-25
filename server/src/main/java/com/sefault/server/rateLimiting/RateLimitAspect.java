package com.sefault.server.rateLimiting;

import com.sefault.server.exception.RateLimitExceededException;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.BucketConfiguration;
import io.github.bucket4j.ConsumptionProbe;
import io.github.bucket4j.distributed.proxy.ProxyManager;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class RateLimitAspect {

    private final ProxyManager<String> proxyManager;
    private final HttpServletRequest request;

    private final ExpressionParser spelParser = new SpelExpressionParser();
    private final Map<String, Expression> spelCache = new ConcurrentHashMap<>();
    private final Map<String, BucketConfiguration> configCache = new ConcurrentHashMap<>();

    @Around("@within(RateLimit) || @annotation(RateLimit)")
    public Object enforceRateLimit(ProceedingJoinPoint joinPoint) throws Throwable {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        RateLimit rateLimit = getRateLimitAnnotation(joinPoint, signature);

        if (rateLimit == null) {
            return joinPoint.proceed();
        }

        String action = rateLimit.actionName();
        String resolvedKey = resolveKey(joinPoint, signature, rateLimit.keyExpression());
        String finalKey = action + ":" + resolvedKey;

        BucketConfiguration config = configCache.computeIfAbsent(action, k -> buildBucketConfig(rateLimit));

        ConsumptionProbe probe =
                proxyManager.builder().build(finalKey, () -> config).tryConsumeAndReturnRemaining(1);

        if (probe.isConsumed()) {
            addRateLimitHeaders(rateLimit.capacity(), probe.getRemainingTokens(), null);
            return joinPoint.proceed();
        }

        long waitForRefillSeconds = probe.getNanosToWaitForRefill() / 1_000_000_000;
        addRateLimitHeaders(rateLimit.capacity(), 0L, waitForRefillSeconds);

        log.warn("Rate limit exceeded for key: {}. Retry in {}s", finalKey, waitForRefillSeconds);
        throw new RateLimitExceededException(waitForRefillSeconds);
    }

    private RateLimit getRateLimitAnnotation(ProceedingJoinPoint joinPoint, MethodSignature signature) {
        RateLimit rateLimit = AnnotationUtils.findAnnotation(signature.getMethod(), RateLimit.class);
        return rateLimit != null
                ? rateLimit
                : AnnotationUtils.findAnnotation(joinPoint.getTarget().getClass(), RateLimit.class);
    }

    private BucketConfiguration buildBucketConfig(RateLimit rateLimit) {
        return BucketConfiguration.builder()
                .addLimit(Bandwidth.builder()
                        .capacity(rateLimit.capacity())
                        .refillGreedy(rateLimit.tokensPerMinute(), Duration.ofMinutes(1))
                        .build())
                .build();
    }

    private void addRateLimitHeaders(int limit, long remaining, Long retryAfterSeconds) {
        try {
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null && attributes.getResponse() != null) {
                HttpServletResponse response = attributes.getResponse();
                response.setHeader("X-RateLimit-Limit", String.valueOf(limit));
                response.setHeader("X-RateLimit-Remaining", String.valueOf(remaining));
                if (retryAfterSeconds != null) {
                    response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));
                }
            }
        } catch (Exception e) {
            log.debug("Unable to set Rate Limit headers", e);
        }
    }

    private String resolveKey(ProceedingJoinPoint joinPoint, MethodSignature signature, String keyExpression) {
        if (StringUtils.hasText(keyExpression)) {
            return evaluateSpelExpression(joinPoint, signature, keyExpression);
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            return authentication.getName();
        }

        return getClientIP();
    }

    private String evaluateSpelExpression(
            ProceedingJoinPoint joinPoint, MethodSignature signature, String keyExpression) {
        try {
            StandardEvaluationContext context = new StandardEvaluationContext();

            String[] parameterNames = signature.getParameterNames();
            Object[] args = joinPoint.getArgs();
            if (parameterNames != null) {
                for (int i = 0; i < parameterNames.length; i++) {
                    context.setVariable(parameterNames[i], args[i]);
                }
            }

            context.setVariable("clientIp", getClientIP());

            Expression expression = spelCache.computeIfAbsent(keyExpression, spelParser::parseExpression);
            String evaluatedValue = expression.getValue(context, String.class);

            return StringUtils.hasText(evaluatedValue) ? evaluatedValue : getClientIP();
        } catch (Exception e) {
            log.error("SpEL evaluation failed for expression: {}. Fallback to IP.", keyExpression, e);
            return getClientIP();
        }
    }

    private String getClientIP() {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (StringUtils.hasText(xfHeader)) {
            return xfHeader.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
