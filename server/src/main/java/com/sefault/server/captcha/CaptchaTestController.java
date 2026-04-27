package com.sefault.server.captcha;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/test")
public class CaptchaTestController {

    private final RecaptchaService recaptchaService;

    public CaptchaTestController(RecaptchaServiceImpl recaptchaService) {
        this.recaptchaService = recaptchaService;
    }

    @PostMapping("/captcha")
    public ResponseEntity<String> testCaptcha(@RequestHeader("X-Captcha-Token") String token) {
        boolean isValid = recaptchaService.verifyCaptcha(token);

        if (isValid) {
            return ResponseEntity.ok("Success: Human verified.");
        } else {
            return ResponseEntity.badRequest().body("Failure: Bot detected or invalid token.");
        }
    }
}
