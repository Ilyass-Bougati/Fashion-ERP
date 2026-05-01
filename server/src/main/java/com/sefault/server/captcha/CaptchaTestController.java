package com.sefault.server.captcha;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Profile({"dev", "staging"})
@RestController
@RequestMapping("/api/test")
@RequiredArgsConstructor
public class CaptchaTestController {

    private final RecaptchaService recaptchaService;

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
