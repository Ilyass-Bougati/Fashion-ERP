package com.sefault.server.captcha;

public interface RecaptchaService {
    boolean verifyCaptcha(String token);
}
