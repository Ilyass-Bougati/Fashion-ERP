package com.sefault.server.email;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class EmailServiceTest {

    @Autowired
    private EmailService emailService;

    @Test
    void TestEnvoiMail() throws InterruptedException {
        Map<String, Object> variables = Map.of("username", "LHACHMI");
        // So here, you can just add your email instead of ****** and you can see the email sent to your own inbox
        EmailRequest request = new EmailRequest("*******@gmail.com", "Test - Fashion ERP", "test-email", variables);

        emailService.sendEmail(request);

        Thread.sleep(5000);
    }
}
