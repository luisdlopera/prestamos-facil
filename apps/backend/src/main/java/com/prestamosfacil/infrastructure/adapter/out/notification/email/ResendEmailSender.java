package com.prestamosfacil.infrastructure.adapter.out.notification.email;

import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

/**
 * Sends email over HTTPS via the Resend API instead of raw SMTP.
 * Needed because several free hosts (e.g. Render's free web service tier)
 * block outbound SMTP ports (25/465/587) to prevent spam abuse, so
 * JavaMailSender connections to smtp.gmail.com time out there.
 */
@Component
public class ResendEmailSender {

    private static final Logger log = LoggerFactory.getLogger(ResendEmailSender.class);
    private static final String RESEND_API_URL = "https://api.resend.com/emails";

    @Value("${app.mail.resend-api-key:}")
    private String apiKey;

    private final RestTemplate restTemplate;

    public ResendEmailSender() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(10_000);
        requestFactory.setReadTimeout(15_000);
        this.restTemplate = new RestTemplate(requestFactory);
    }

    public boolean isEnabled() {
        return apiKey != null && !apiKey.isBlank();
    }

    public void send(String from, String to, String subject, String html) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setBearerAuth(apiKey);

        Map<String, Object> body = Map.of(
            "from", from,
            "to", to,
            "subject", subject,
            "html", html
        );

        restTemplate.postForEntity(RESEND_API_URL, new HttpEntity<>(body, headers), String.class);
        log.info("Email sent via Resend to {}: {}", to, subject);
    }
}
