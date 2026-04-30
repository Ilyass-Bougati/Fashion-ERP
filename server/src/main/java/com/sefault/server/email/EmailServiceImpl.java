package com.sefault.server.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String senderEmail;

    @Async("taskExecutor")
    @Override
    public void sendEmail(EmailRequest request) {
        log.info("Processing email for {} (Template: {})", request.to(), request.templateName());

        try {
            Context context = new Context();
            context.setVariables(request.variables());

            String htmlContent = templateEngine.process("emails/" + request.templateName(), context);

            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(senderEmail);
            helper.setTo(request.to());
            helper.setSubject(request.subject());
            helper.setText(htmlContent, true);

            javaMailSender.send(message);
            log.debug("Email successfully sent to {}", request.to());

        } catch (MailException | MessagingException e) {
            log.error("Failed to send email to {}. Reason: {}", request.to(), e.getMessage());
        }
    }
}
