package com.sefault.server.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("EmailServiceImpl Unit Tests")
class EmailServiceImplTest {

    @Mock
    private JavaMailSender javaMailSender;

    @Mock
    private TemplateEngine templateEngine;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private EmailServiceImpl emailService;

    private static final String SENDER_EMAIL = "noreply@sefault.com";
    private static final String RECIPIENT_EMAIL = "user@example.com";
    private static final String TEMPLATE_NAME = "welcome";
    private static final String SUBJECT = "Welcome!";
    private static final String RENDERED_HTML = "<html><body>Hello!</body></html>";

    @BeforeEach
    void setUp() {
        // Inject the @Value field since we're not loading a Spring context
        ReflectionTestUtils.setField(emailService, "senderEmail", SENDER_EMAIL);
    }

    // -------------------------------------------------------------------------
    // Helper
    // -------------------------------------------------------------------------

    private EmailRequest buildRequest(Map<String, Object> variables) {
        return new EmailRequest(RECIPIENT_EMAIL, SUBJECT, TEMPLATE_NAME, variables);
    }

    // -------------------------------------------------------------------------
    // Happy-path tests
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("sendEmail — processes the correct Thymeleaf template path")
    void sendEmail_processesCorrectTemplatePath() {
        EmailRequest request = buildRequest(Map.of("name", "Alice"));

        when(templateEngine.process(anyString(), any(Context.class))).thenReturn(RENDERED_HTML);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailService.sendEmail(request);

        verify(templateEngine).process(eq("emails/" + TEMPLATE_NAME), any(Context.class));
    }

    @Test
    @DisplayName("sendEmail — passes all request variables to the Thymeleaf context")
    void sendEmail_setsContextVariables() {
        Map<String, Object> variables = Map.of("name", "Alice", "token", "abc123");
        EmailRequest request = buildRequest(variables);

        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
        when(templateEngine.process(anyString(), contextCaptor.capture())).thenReturn(RENDERED_HTML);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailService.sendEmail(request);

        Context capturedContext = contextCaptor.getValue();
        variables.forEach((key, value) ->
                org.assertj.core.api.Assertions.assertThat(capturedContext.getVariable(key)).isEqualTo(value));
    }

    @Test
    @DisplayName("sendEmail — creates a MimeMessage and sends it exactly once")
    void sendEmail_sendsExactlyOneMessage() {
        EmailRequest request = buildRequest(Map.of());

        when(templateEngine.process(anyString(), any(Context.class))).thenReturn(RENDERED_HTML);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailService.sendEmail(request);

        verify(javaMailSender, times(1)).createMimeMessage();
        verify(javaMailSender, times(1)).send(mimeMessage);
    }

    @Test
    @DisplayName("sendEmail — sends no real emails (JavaMailSender is always mocked)")
    void sendEmail_neverSendsRealEmails() {
        // Confirm the mock is never replaced by a real implementation
        EmailRequest request = buildRequest(Map.of());

        when(templateEngine.process(anyString(), any(Context.class))).thenReturn(RENDERED_HTML);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailService.sendEmail(request);

        // Verify send was called on the mock (not a real SMTP connection)
        verify(javaMailSender).send(any(MimeMessage.class));
        verifyNoMoreInteractions(javaMailSender);
    }

    @Test
    @DisplayName("sendEmail — works correctly with empty variables map")
    void sendEmail_handlesEmptyVariables() {
        EmailRequest request = buildRequest(Map.of());

        when(templateEngine.process(anyString(), any(Context.class))).thenReturn(RENDERED_HTML);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        assertThatCode(() -> emailService.sendEmail(request)).doesNotThrowAnyException();
    }

    // -------------------------------------------------------------------------
    // Error-handling tests
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("sendEmail — swallows MailException without rethrowing")
    void sendEmail_swallowsMailException() {
        EmailRequest request = buildRequest(Map.of());

        when(templateEngine.process(anyString(), any(Context.class))).thenReturn(RENDERED_HTML);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new MailSendException("SMTP refused")).when(javaMailSender).send(any(MimeMessage.class));

        // Must NOT propagate — the service catches and logs it
        assertThatCode(() -> emailService.sendEmail(request)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("sendEmail — swallows MessagingException without rethrowing")
    void sendEmail_swallowsMessagingException() {
        EmailRequest request = buildRequest(Map.of());

        when(templateEngine.process(anyString(), any(Context.class))).thenReturn(RENDERED_HTML);
        // Simulate MimeMessage setup failure
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        doThrow(new MailSendException("Messaging error",
                new MessagingException("Bad address")))
                .when(javaMailSender).send(any(MimeMessage.class));

        assertThatCode(() -> emailService.sendEmail(request)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("sendEmail — does not call send() when template processing fails")
    void sendEmail_doesNotSendWhenTemplateThrows() {
        EmailRequest request = buildRequest(Map.of());

        when(templateEngine.process(anyString(), any(Context.class)))
                .thenThrow(new RuntimeException("Template not found"));

        // The RuntimeException is not caught by the service → propagates
        // Confirm send() is never reached
        assertThatCode(() -> emailService.sendEmail(request))
                .isInstanceOf(RuntimeException.class);

        verify(javaMailSender, never()).send(any(MimeMessage.class));
    }

    // -------------------------------------------------------------------------
    // Interaction / no-extra-calls tests
    // -------------------------------------------------------------------------

    @Test
    @DisplayName("sendEmail — does not interact with TemplateEngine after sending")
    void sendEmail_noExtraTemplateEngineInteractions() {
        EmailRequest request = buildRequest(Map.of("key", "value"));

        when(templateEngine.process(anyString(), any(Context.class))).thenReturn(RENDERED_HTML);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailService.sendEmail(request);

        verify(templateEngine, times(1)).process(anyString(), any(Context.class));
        verifyNoMoreInteractions(templateEngine);
    }

    @Test
    @DisplayName("sendEmail — calls createMimeMessage before send, never after")
    void sendEmail_createsMessageBeforeSending() {
        EmailRequest request = buildRequest(Map.of());

        when(templateEngine.process(anyString(), any(Context.class))).thenReturn(RENDERED_HTML);
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);

        emailService.sendEmail(request);

        // Strict order: create → send
        var inOrder = inOrder(javaMailSender);
        inOrder.verify(javaMailSender).createMimeMessage();
        inOrder.verify(javaMailSender).send(mimeMessage);
    }
}