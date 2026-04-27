package com.sefault.server.captcha;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Bean
    public RestClient recaptchaRestClient(RestClient.Builder builder) {
        return builder.baseUrl("https://www.google.com/recaptcha/api/siteverify")
                .build();
    }
}
