package com.sefault.server.email;

import org.springframework.scheduling.annotation.Async;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    @Async
    @Override
    public void sendEmail(EmailRequest emailRequest) {
        // TODO: Implementation of email sending
        log.info("Sending email to {} (Template: {})",emailRequest.to(),emailRequest.templateName());
    }
}
