package com.prestamosfacil.infrastructure.adapter.out.notification;

import com.prestamosfacil.domain.notification.models.Notification;
import com.prestamosfacil.domain.notification.port.out.NotificationPort;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("!email")
public class LogNotificationAdapter implements NotificationPort {

    private static final Logger log = LoggerFactory.getLogger(LogNotificationAdapter.class);

    @Override
    public void notifyApplicationReceived(Notification data) {
        log.info("[NOTIFICACIÓN] Solicitud {} recibida para cliente {} {} ({})",
            data.getApplicationId(), data.getCustomer().getFirstName(), data.getCustomer().getLastName(),
            data.getCustomer().getEmail().getValue());
    }

    @Override
    public void notifyApplicationApproved(Notification data) {
        log.info("[NOTIFICACIÓN] Solicitud {} APROBADA para cliente {} {} ({})",
            data.getApplicationId(), data.getCustomer().getFirstName(), data.getCustomer().getLastName(),
            data.getCustomer().getEmail().getValue());
    }

    @Override
    public void notifyApplicationRejected(Notification data) {
        log.info("[NOTIFICACIÓN] Solicitud {} RECHAZADA para cliente {} {} ({}). Motivo: {}",
            data.getApplicationId(), data.getCustomer().getFirstName(), data.getCustomer().getLastName(),
            data.getCustomer().getEmail().getValue(), data.getReason());
    }

    @Override
    public void notifyManualReview(Notification data) {
        log.info("[NOTIFICACIÓN] Solicitud {} enviada a REVISIÓN MANUAL para cliente {} {} ({})",
            data.getApplicationId(), data.getCustomer().getFirstName(), data.getCustomer().getLastName(),
            data.getCustomer().getEmail().getValue());
    }

    @Override
    public void notifyPasswordResetRequested(String toEmail, String resetLink) {
        log.info("[NOTIFICACIÓN] Restablecimiento de contraseña solicitado para {}: {}", toEmail, resetLink);
    }
}
