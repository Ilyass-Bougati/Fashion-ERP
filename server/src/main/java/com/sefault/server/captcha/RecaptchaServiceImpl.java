package com.sefault.server.captcha;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

@Service
@EnableConfigurationProperties(RecaptchaProperties.class)
public class RecaptchaServiceImpl implements RecaptchaService {
    private final RecaptchaProperties recaptchaProperties;
    private final RestClient restClient;

    public RecaptchaServiceImpl(RecaptchaProperties recaptchaProperties) {
        this.recaptchaProperties = recaptchaProperties;

        this.restClient = RestClient.builder()
                .baseUrl("https://www.google.com/recaptcha/api/siteverify")
                .build();
    }

    public boolean verifyCaptcha(String token) {
        if (token == null || token.isEmpty()) {
            return false;
        }

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("secret", recaptchaProperties.secretKey());
        body.add("response", token);

        RecaptchaResponse response = restClient.post().body(body).retrieve().body(RecaptchaResponse.class);

        return response != null && response.success();
    }
}
