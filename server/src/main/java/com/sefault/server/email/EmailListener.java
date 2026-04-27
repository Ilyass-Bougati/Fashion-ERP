package com.sefault.server.email;

import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class EmailListener {

    @Async
    @EventListener
    public void handleEmailEvent(SendEmailEvent event) {
        // TODO: Implementation of email sending
        log.info("Processing email dispatch. Template: [{}], Recipient: [{}]", event.templateName(), event.to());
    }
}
