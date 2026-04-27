package com.sefault.server.captcha;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "recaptcha")
public record RecaptchaProperties(String secretKey) {}
