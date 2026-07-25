package com.prestamosfacil.application.loanapplication;

import com.prestamosfacil.domain.customer.models.Customer;
import com.prestamosfacil.domain.customer.port.out.CustomerRepository;
import com.prestamosfacil.domain.shared.enums.Messages;
import com.prestamosfacil.domain.loan.models.Loan;
import com.prestamosfacil.domain.loan.port.in.LoanUseCase;
import com.prestamosfacil.domain.loanapplication.enums.LoanApplicationStatus;
import com.prestamosfacil.domain.loanapplication.enums.ManualDecision;
import com.prestamosfacil.domain.loanapplication.exceptions.InvalidLoanApplicationStateException;
import com.prestamosfacil.domain.loanapplication.event.ApplicationApprovedEvent;
import com.prestamosfacil.domain.loanapplication.event.ApplicationReceivedEvent;
import com.prestamosfacil.domain.loanapplication.event.ApplicationRejectedEvent;
import com.prestamosfacil.domain.loanapplication.event.ManualReviewRequiredEvent;
import com.prestamosfacil.domain.loanapplication.models.AutomaticEvaluationOutcome;
import com.prestamosfacil.domain.loanapplication.models.LoanApplication;
import com.prestamosfacil.domain.loanapplication.port.in.LoanApplicationUseCase;
import com.prestamosfacil.domain.loanapplication.models.EvaluationResult;
import com.prestamosfacil.domain.loanapplication.port.out.AutomaticLoanEvaluationPort;
import com.prestamosfacil.domain.loanapplication.port.out.LoanApplicationRepository;
import com.prestamosfacil.domain.loantype.models.LoanType;
import com.prestamosfacil.domain.loantype.port.out.LoanTypeRepository;
import com.prestamosfacil.domain.paymentplan.models.PaymentInstallment;
import com.prestamosfacil.domain.paymentplan.port.in.PaymentPlanUseCase;
import com.prestamosfacil.domain.shared.Money;
import com.prestamosfacil.domain.shared.PageResult;
import com.prestamosfacil.domain.shared.exception.NotFoundException;
import com.prestamosfacil.domain.shared.port.out.EventPublisher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class LoanApplicationUseCaseImpl implements LoanApplicationUseCase {

    private static final Logger log = LoggerFactory.getLogger(LoanApplicationUseCaseImpl.class);

    private final LoanApplicationRepository loanApplicationRepository;
    private final CustomerRepository customerRepository;
    private final LoanTypeRepository loanTypeRepository;
    private final AutomaticLoanEvaluationPort evaluationPort;
    private final LoanUseCase loanUseCase;
    private final PaymentPlanUseCase paymentPlanUseCase;
    private final EventPublisher eventPublisher;
    private LoanApplicationUseCaseImpl self;

    public LoanApplicationUseCaseImpl(LoanApplicationRepository loanApplicationRepository,
                                      CustomerRepository customerRepository,
                                      LoanTypeRepository loanTypeRepository,
                                      AutomaticLoanEvaluationPort evaluationPort,
                                      LoanUseCase loanUseCase,
                                      PaymentPlanUseCase paymentPlanUseCase,
                                      EventPublisher eventPublisher) {
        this.loanApplicationRepository = loanApplicationRepository;
        this.customerRepository = customerRepository;
        this.loanTypeRepository = loanTypeRepository;
        this.evaluationPort = evaluationPort;
        this.loanUseCase = loanUseCase;
        this.paymentPlanUseCase = paymentPlanUseCase;
        this.eventPublisher = eventPublisher;
        this.self = this;
    }

    // Self-reference re-injected as the Spring AOP proxy so calls to
    // runAutomaticEvaluation() from within this class go through the proxy
    // and its @Transactional boundary actually applies (self-invocation
    // otherwise bypasses the proxy).
    @Autowired
    public void setSelf(@Lazy LoanApplicationUseCaseImpl self) {
        this.self = self;
    }

    @Override
    @Transactional
    public LoanApplication createApplication(UUID customerId, UUID loanTypeId,
                                              BigDecimal requestedAmount, int termInMonths) {
        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new NotFoundException(Messages.CUSTOMER_NOT_FOUND_ID.format(customerId)));
        LoanType loanType = loanTypeRepository.findById(loanTypeId)
            .orElseThrow(() -> new NotFoundException(Messages.LOAN_TYPE_NOT_FOUND_ID.format(loanTypeId)));

        Money money = new Money(requestedAmount);

        loanType.validateApplicationRequest(money, termInMonths);

        LoanApplication application = new LoanApplication(customer, loanType, money, termInMonths);
        application = loanApplicationRepository.save(application);

        eventPublisher.publish(new ApplicationReceivedEvent(
            application.getId(),
            customer.getId(),
            customer.getFirstName() + " " + customer.getLastName(),
            customer.getEmail() != null ? customer.getEmail().getValue() : null,
            loanType.getName(),
            money.getAmount(),
            termInMonths));

        if (loanType.isAutomaticValidationEnabled()) {
            UUID savedApplicationId = application.getId();
            log.info("Solicitud={} creada para un tipo de préstamo con validación automática habilitada; evaluando", savedApplicationId);
            self.runAutomaticEvaluation(application);
            application = loanApplicationRepository.findById(savedApplicationId).orElse(application);
            log.info("Solicitud={} resultado de evaluación: status={}", savedApplicationId, application.getStatus());
        }

        return application;
    }

    @Override
    public Optional<LoanApplication> findById(UUID id) {
        return loanApplicationRepository.findById(id);
    }

    @Override
    public Optional<LoanApplication> findByIdForUser(UUID applicationId, UUID customerId) {
        return loanApplicationRepository.findById(applicationId)
            .filter(app -> app.getCustomer().getId().equals(customerId));
    }

    @Override
    public PageResult<LoanApplication> findAll(int page, int perPage, String sortBy, String sortDir) {
        return loanApplicationRepository.findAll(page, perPage, sortBy, sortDir);
    }

    @Override
    public PageResult<LoanApplication> findByFilters(UUID customerId, String statusCode, String search, int page, int perPage, String sortBy, String sortDir) {
        return loanApplicationRepository.findByFilters(customerId, statusCode, search, page, perPage, sortBy, sortDir);
    }

    @Override
    @Transactional
    public LoanApplication manualDecision(UUID applicationId, ManualDecision decision, String reason) {
        LoanApplication application = loanApplicationRepository.findById(applicationId)
            .orElseThrow(() -> new NotFoundException(Messages.LOAN_APPLICATION_NOT_FOUND_ID.format(applicationId)));

        if (!application.canBeDecided()) {
            throw new InvalidLoanApplicationStateException(
                Messages.LOAN_APPLICATION_CANNOT_DECIDE.format(application.getStatus()));
        }

        if (decision == ManualDecision.APPROVE) {
            return approveWithPaymentPlan(application);
        } else {
            return rejectApplication(application, reason);
        }
    }

    @Override
    @Transactional
    public LoanApplication approve(UUID id) {
        return manualDecision(id, ManualDecision.APPROVE, null);
    }

    @Override
    @Transactional
    public LoanApplication reject(UUID id, String reason) {
        return manualDecision(id, ManualDecision.REJECT, reason);
    }

    @Override
    @Transactional
    public AutomaticEvaluationOutcome evaluateAutomatically(UUID applicationId) {
        LoanApplication application = loanApplicationRepository.findById(applicationId)
            .orElseThrow(() -> new NotFoundException(Messages.LOAN_APPLICATION_NOT_FOUND_ID.format(applicationId)));

        if (application.getStatus() != LoanApplicationStatus.PENDING_REVIEW) {
            throw new InvalidLoanApplicationStateException(
                Messages.LOAN_APPLICATION_AUTO_EVAL_INVALID_STATE.getValue());
        }

        if (!application.getLoanType().isAutomaticValidationEnabled()) {
            throw new IllegalArgumentException(
                Messages.LOAN_TYPE_AUTO_VALIDATION_DISABLED.getValue());
        }

        return self.runAutomaticEvaluation(application);
    }

    @Transactional
    public AutomaticEvaluationOutcome runAutomaticEvaluation(LoanApplication application) {
        Customer customer = application.getCustomer();

        EvaluationResult evalResult = evaluationPort.evaluate(
            application.getId(),
            customer.getBaseSalary().getAmount(),
            application.getRequestedAmount().getAmount(),
            application.getTermInMonths(),
            application.getLoanType().getInterestRate());

        log.info("Resultado evaluación automática para solicitud={}: decision={} debtRatio={}",
            application.getId(), evalResult.decision(), evalResult.debtRatio());

        LoanApplicationStatus status = applyAutomaticDecision(application, evalResult);

        return new AutomaticEvaluationOutcome(
            status,
            evalResult.maxCapacity(),
            evalResult.currentDebt(),
            evalResult.newInstallment(),
            evalResult.debtRatio(),
            evalResult.reason());
    }

    private LoanApplicationStatus applyAutomaticDecision(
            LoanApplication application,
            EvaluationResult evalResult) {

        String decision = evalResult.decision();
        String reason = evalResult.reason();

        switch (decision) {
            case "APPROVED" -> {
                LoanApplication updated = application.withDecisionReason(reason);
                approveWithPaymentPlan(updated);
                return LoanApplicationStatus.APPROVED;
            }
            case "MANUAL_REVIEW" -> {
                LoanApplication updated = application.autoEvaluate("MANUAL_REVIEW", reason);
                loanApplicationRepository.save(updated);
                notifyManualReview(updated);
                return LoanApplicationStatus.MANUAL_REVIEW;
            }
            case "REJECTED" -> {
                LoanApplication updated = application.reject(reason);
                loanApplicationRepository.save(updated);
                publishRejected(updated, reason);
                return LoanApplicationStatus.REJECTED;
            }
            default -> throw new IllegalStateException(Messages.LOAN_APPLICATION_UNEXPECTED_SP_DECISION.format(decision));
        }
    }

    private LoanApplication approveWithPaymentPlan(LoanApplication application) {
        LoanApplication approvedApp = application.approve();
        LoanApplication savedApp = loanApplicationRepository.save(approvedApp);
        Loan loan = loanUseCase.createLoanFromApplication(savedApp.getId());
        if (loan != null) {
            List<PaymentInstallment> installments = paymentPlanUseCase.getPaymentPlan(loan.getId());
            Customer customer = savedApp.getCustomer();
            eventPublisher.publish(new ApplicationApprovedEvent(
                savedApp.getId(),
                customer.getId(),
                customer.getFirstName() + " " + customer.getLastName(),
                customer.getEmail() != null ? customer.getEmail().getValue() : null,
                savedApp.getLoanType() != null ? savedApp.getLoanType().getName() : "",
                savedApp.getRequestedAmount() != null ? savedApp.getRequestedAmount().getAmount() : null,
                savedApp.getTermInMonths(),
                installments));
        }
        return savedApp;
    }

    private LoanApplication rejectApplication(LoanApplication application, String reason) {
        application = application.reject(reason);
        application = loanApplicationRepository.save(application);
        publishRejected(application, reason);
        return application;
    }

    private void publishRejected(LoanApplication application, String reason) {
        Customer customer = application.getCustomer();
        eventPublisher.publish(new ApplicationRejectedEvent(
            application.getId(),
            customer.getId(),
            customer.getFirstName() + " " + customer.getLastName(),
            customer.getEmail() != null ? customer.getEmail().getValue() : null,
            application.getLoanType().getName(),
            application.getRequestedAmount().getAmount(),
            application.getTermInMonths(),
            reason));
    }

    private void notifyManualReview(LoanApplication application) {
        Customer customer = application.getCustomer();
        eventPublisher.publish(new ManualReviewRequiredEvent(
            application.getId(),
            customer.getId(),
            customer.getFirstName() + " " + customer.getLastName(),
            customer.getEmail() != null ? customer.getEmail().getValue() : null,
            application.getLoanType().getName(),
            application.getRequestedAmount().getAmount(),
            application.getTermInMonths()));
    }
}
