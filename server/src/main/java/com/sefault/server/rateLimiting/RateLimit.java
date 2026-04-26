package com.sefault.server.rateLimiting;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.intellij.lang.annotations.Language;

@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface RateLimit {

    String actionName();

    int capacity() default 50;

    int tokensPerMinute() default 20;

    @Language("SpEL")
    String keyExpression() default "";
}
