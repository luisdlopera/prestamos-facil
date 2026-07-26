package com.prestamosfacil.infrastructure.adapter.out.notification.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final SpringTemplateEngine templateEngine;

    @Value("${spring.mail.username:noreply@prestamosfacil.local}")
    private String senderEmail;

    public EmailService(JavaMailSender mailSender, SpringTemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    public void send(String to, String subject, EmailTemplateDTO templateDto, String templateName,
                     Map<String, Object> extraVariables) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(
                message,
                MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
                StandardCharsets.UTF_8.name()
            );

            Context context = new Context();
            context.setVariable("title", templateDto.getTitle());
            context.setVariable("subtitle", templateDto.getSubtitle());
            context.setVariable("content", templateDto.getContent());
            context.setVariable("footer", templateDto.getFooter());

            if (templateDto.getAction() != null) {
                context.setVariable("action", templateDto.getAction());
            }

            if (extraVariables != null) {
                for (Map.Entry<String, Object> entry : extraVariables.entrySet()) {
                    context.setVariable(entry.getKey(), entry.getValue());
                }
            }

            String htmlBody = templateEngine.process(templateName, context);

            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);
            helper.setFrom(senderEmail);

            mailSender.send(message);
            log.info("Email sent to {}: {}", to, subject);
        } catch (MessagingException e) {
            throw new RuntimeException("Error sending email to " + to + ": " + e.getMessage(), e);
        }
    }

    public void send(String to, String subject, EmailTemplateDTO templateDto) {
        this.send(to, subject, templateDto, "email/email", null);
    }
}
