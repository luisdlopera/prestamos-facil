package com.prestamosfacil.domain.notification.port.out;

import com.prestamosfacil.domain.notification.models.Notification;

public interface NotificationPort {

    void notifyApplicationReceived(Notification data);

    void notifyApplicationApproved(Notification data);

    void notifyApplicationRejected(Notification data);

    void notifyManualReview(Notification data);

    void notifyPasswordResetRequested(String toEmail, String resetLink);
}
