package com.prestamosfacil.infrastructure.adapter.out.notification;

import com.prestamosfacil.domain.loanapplication.event.ApplicationApprovedEvent;
import com.prestamosfacil.domain.loanapplication.event.ApplicationReceivedEvent;
import com.prestamosfacil.domain.loanapplication.event.ApplicationRejectedEvent;
import com.prestamosfacil.domain.loanapplication.event.ManualReviewRequiredEvent;
import com.prestamosfacil.domain.customer.models.Customer;
import com.prestamosfacil.domain.customer.port.out.CustomerRepository;
import com.prestamosfacil.domain.notification.models.Notification;
import com.prestamosfacil.domain.notification.port.out.NotificationPort;
import com.prestamosfacil.domain.shared.exception.NotFoundException;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class LoanApplicationNotificationsListener {

    private final NotificationPort notificationPort;
    private final CustomerRepository customerRepository;

    public LoanApplicationNotificationsListener(NotificationPort notificationPort,
                                                CustomerRepository customerRepository) {
        this.notificationPort = notificationPort;
        this.customerRepository = customerRepository;
    }

    @EventListener
    public void onApplicationReceived(ApplicationReceivedEvent event) {
        Customer customer = findCustomer(event.customerId());

        Notification data = Notification.builder()
            .customer(customer)
            .applicationId(event.applicationId().toString())
            .loanTypeName(event.loanTypeName())
            .requestedAmount(event.requestedAmount())
            .termInMonths(event.termInMonths())
            .build();
        notificationPort.notifyApplicationReceived(data);
    }

    @EventListener
    public void onApplicationApproved(ApplicationApprovedEvent event) {
        Customer customer = findCustomer(event.customerId());

        Notification data = Notification.builder()
            .customer(customer)
            .applicationId(event.applicationId().toString())
            .loanTypeName(event.loanTypeName())
            .requestedAmount(event.requestedAmount())
            .termInMonths(event.termInMonths())
            .installments(event.installments())
            .build();
        notificationPort.notifyApplicationApproved(data);
    }

    @EventListener
    public void onApplicationRejected(ApplicationRejectedEvent event) {
        Customer customer = findCustomer(event.customerId());

        Notification data = Notification.builder()
            .customer(customer)
            .applicationId(event.applicationId().toString())
            .loanTypeName(event.loanTypeName())
            .requestedAmount(event.requestedAmount())
            .termInMonths(event.termInMonths())
            .reason(event.reason())
            .build();
        notificationPort.notifyApplicationRejected(data);
    }

    @EventListener
    public void onManualReviewRequired(ManualReviewRequiredEvent event) {
        Customer customer = findCustomer(event.customerId());

        Notification data = Notification.builder()
            .customer(customer)
            .applicationId(event.applicationId().toString())
            .loanTypeName(event.loanTypeName())
            .requestedAmount(event.requestedAmount())
            .termInMonths(event.termInMonths())
            .build();
        notificationPort.notifyManualReview(data);
    }

    private Customer findCustomer(UUID customerId) {
        return customerRepository.findById(customerId)
            .orElseThrow(() -> new NotFoundException("Customer not found: " + customerId));
    }
}
