package com.prestamosfacil.infrastructure.adapter.out.notification;

import com.prestamosfacil.domain.notification.models.Notification;
import com.prestamosfacil.domain.notification.port.out.NotificationPort;
import com.prestamosfacil.domain.shared.enums.Messages;
import com.prestamosfacil.infrastructure.adapter.out.notification.email.EmailService;
import com.prestamosfacil.infrastructure.adapter.out.notification.email.EmailTemplateDTO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

@Component
@Profile("email")
public class EmailNotificationAdapter implements NotificationPort {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationAdapter.class);

    private final EmailService emailService;
    private final SpringTemplateEngine templateEngine;

    @Value("${app.frontend-url}")
    private String clientUrl;

    public EmailNotificationAdapter(EmailService emailService, SpringTemplateEngine templateEngine) {
        this.emailService = emailService;
        this.templateEngine = templateEngine;
    }

    @Override
    public void notifyApplicationReceived(Notification data) {
        sendNotification(data, "email/fragments/application-received", "email/email",
            ctx -> {
                ctx.put("applicationId", data.getApplicationId() != null ? data.getApplicationId().substring(0, 8) + "..." : "");
            },
            vars -> {},
            Messages.EMAIL_SUBJECT_RECEIVED.getValue(),
            Messages.EMAIL_TITLE_RECEIVED.getValue(),
            Messages.EMAIL_SUBTITLE_RECEIVED.getValue(),
            Messages.EMAIL_BUTTON_VIEW_DETAILS.getValue(),
            null);
    }

    @Override
    public void notifyApplicationApproved(Notification data) {
        sendNotification(data, "email/fragments/application-approved", "email/loan-notification",
            ctx -> {},
            vars -> {
                vars.put("status", Messages.EMAIL_STATUS_APPROVED.getValue());
                if (data.getInstallments() != null && !data.getInstallments().isEmpty()) {
                    var formattedInstallments = data.getInstallments().stream()
                        .map(inst -> {
                            Map<String, Object> map = new HashMap<>();
                            map.put("installmentNumber", inst.getInstallmentNumber());
                            map.put("dueDate", inst.getDueDate() != null ? inst.getDueDate().toString() : "");
                            map.put("openingBalance", formatCurrency(inst.getOpeningBalance() != null ? inst.getOpeningBalance().getAmount() : null));
                            map.put("paymentAmount", formatCurrency(inst.getPaymentAmount() != null ? inst.getPaymentAmount().getAmount() : null));
                            map.put("principalAmount", formatCurrency(inst.getPrincipalAmount() != null ? inst.getPrincipalAmount().getAmount() : null));
                            map.put("interestAmount", formatCurrency(inst.getInterestAmount() != null ? inst.getInterestAmount().getAmount() : null));
                            map.put("closingBalance", formatCurrency(inst.getClosingBalance() != null ? inst.getClosingBalance().getAmount() : null));
                            map.put("totalAmount", formatCurrency(inst.getPaymentAmount() != null ? inst.getPaymentAmount().getAmount() : null));
                            return map;
                        })
                        .toList();
                    vars.put("installments", formattedInstallments);
                    log.info("Including {} installments in approval email for application={}", formattedInstallments.size(), data.getApplicationId());
                } else {
                    log.warn("No installments found for approval email for application={}", data.getApplicationId());
                }
            },
            Messages.EMAIL_SUBJECT_APPROVED.getValue(),
            Messages.EMAIL_TITLE_APPROVED.getValue(),
            Messages.EMAIL_SUBTITLE_APPROVED.getValue(),
            Messages.EMAIL_BUTTON_VIEW_DETAILS.getValue(),
            null);
    }

    @Override
    public void notifyApplicationRejected(Notification data) {
        sendNotification(data, "email/fragments/application-rejected", "email/loan-notification",
            ctx -> {
                ctx.put("reason", data.getReason() != null ? data.getReason() : Messages.EMAIL_DEFAULT_REJECT_REASON.getValue());
            },
            vars -> {
                vars.put("status", Messages.EMAIL_STATUS_REJECTED.getValue());
            },
            Messages.EMAIL_SUBJECT_REJECTED.getValue(),
            Messages.EMAIL_TITLE_REJECTED.getValue(),
            Messages.EMAIL_SUBTITLE_REJECTED.getValue(),
            Messages.EMAIL_BUTTON_VIEW_STATUS.getValue(),
            data.getReason());
    }

    @Override
    public void notifyManualReview(Notification data) {
        sendNotification(data, "email/fragments/manual-review", "email/loan-notification",
            ctx -> {},
            vars -> {
                vars.put("status", Messages.EMAIL_STATUS_MANUAL_REVIEW.getValue());
            },
            Messages.EMAIL_SUBJECT_MANUAL_REVIEW.getValue(),
            Messages.EMAIL_TITLE_MANUAL_REVIEW.getValue(),
            Messages.EMAIL_SUBTITLE_MANUAL_REVIEW.getValue(),
            Messages.EMAIL_BUTTON_VIEW_STATUS.getValue(),
            null);
    }

    private void sendNotification(Notification data, String fragment, String baseTemplate,
                                   Consumer<Map<String, Object>> ctxCustomizer,
                                   Consumer<Map<String, Object>> varsCustomizer,
                                   String subject, String title, String subtitle,
                                   String buttonLabel, String reason) {
        if (data.getCustomer() == null || data.getCustomer().getEmail() == null) {
            throw new IllegalStateException(
                "Cannot send " + fragment + " email: customer or email is null for application=" + data.getApplicationId());
        }
        try {
            String to = data.getCustomer().getEmail().getValue();
            String customerName = data.getCustomer().getFirstName() + " " + data.getCustomer().getLastName();

            Map<String, Object> ctx = new HashMap<>();
            ctx.put("customerName", customerName);
            ctx.put("loanTypeName", data.getLoanTypeName());
            ctx.put("amount", formatCurrency(data.getRequestedAmount()));
            ctxCustomizer.accept(ctx);
            String content = processFragment(fragment, ctx);

            EmailTemplateDTO.ActionDto actionDto = (data.getApplicationId() != null)
                ? new EmailTemplateDTO.ActionDto(buttonLabel, clientUrl + "/loan-details?id=" + data.getApplicationId())
                : null;

            EmailTemplateDTO dto = new EmailTemplateDTO(
                title, subtitle, content, Messages.EMAIL_FOOTER.getValue(), actionDto);

            Map<String, Object> vars = buildExtraVars(data, reason);
            varsCustomizer.accept(vars);

            emailService.send(to, subject, dto, baseTemplate, vars);
            log.info("Email sent to {}: {} ({})", to, subject, data.getApplicationId());
        } catch (Exception e) {
            throw new RuntimeException(
                "Error sending " + fragment + " email for application=" + data.getApplicationId() + ": " + e.getMessage(), e);
        }
    }

    @Override
    public void notifyPasswordResetRequested(String toEmail, String resetLink) {
        EmailTemplateDTO.ActionDto action = new EmailTemplateDTO.ActionDto("Restablecer contraseña", resetLink);
        EmailTemplateDTO dto = new EmailTemplateDTO(
            "Restablecer contraseña",
            "Protege tu cuenta con una nueva contraseña",
            "Para restablecer tu contraseña, haz clic en el botón de abajo. Por tu seguridad, este enlace es válido por 1 hora.",
            "¿No solicitaste este cambio? No te preocupes, tu contraseña permanece segura. Simplemente ignora este correo.",
            action);
        emailService.send(toEmail, "Restablecimiento de contraseña", dto);
    }

    private String processFragment(String templateName, Map<String, Object> variables) {
        Context context = new Context();
        variables.forEach(context::setVariable);
        return templateEngine.process(templateName, context);
    }

    private Map<String, Object> buildExtraVars(Notification data, String reason) {
        Map<String, Object> vars = new HashMap<>();
        vars.put("loanTypeName", data.getLoanTypeName());
        vars.put("amount", formatCurrency(data.getRequestedAmount()));
        vars.put("term", data.getTermInMonths());
        if (reason != null) {
            vars.put("reason", reason);
        }
        return vars;
    }

    private static String formatCurrency(Object amount) {
        if (amount == null) return "$0";
        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.of("es", "CO"));
        return formatter.format(amount);
    }
}
