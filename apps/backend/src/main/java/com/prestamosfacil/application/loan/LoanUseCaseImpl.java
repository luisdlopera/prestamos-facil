package com.prestamosfacil.application.loan;

import com.prestamosfacil.domain.loan.models.LoanCalculator;
import com.prestamosfacil.domain.loan.port.in.LoanUseCase;
import com.prestamosfacil.domain.paymentplan.port.in.PaymentPlanUseCase;
import com.prestamosfacil.domain.loan.models.Loan;
import com.prestamosfacil.domain.loan.port.out.LoanRepository;
import com.prestamosfacil.domain.loanapplication.models.LoanApplication;
import com.prestamosfacil.domain.loanapplication.port.out.LoanApplicationRepository;
import com.prestamosfacil.domain.loanapplication.enums.LoanApplicationStatus;
import com.prestamosfacil.domain.shared.Money;
import com.prestamosfacil.domain.shared.PageResult;
import com.prestamosfacil.domain.shared.enums.Messages;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import java.util.UUID;

import com.prestamosfacil.domain.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoanUseCaseImpl implements LoanUseCase {

    private final LoanRepository loanRepository;
    private final LoanApplicationRepository loanApplicationRepository;
    private final PaymentPlanUseCase paymentPlanUseCase;
    private final LoanCalculator loanCalculator;

    public LoanUseCaseImpl(LoanRepository loanRepository,
                           LoanApplicationRepository loanApplicationRepository,
                           PaymentPlanUseCase paymentPlanUseCase,
                           LoanCalculator loanCalculator) {
        this.loanRepository = loanRepository;
        this.loanApplicationRepository = loanApplicationRepository;
        this.paymentPlanUseCase = paymentPlanUseCase;
        this.loanCalculator = loanCalculator;
    }

    @Override
    @Transactional
    public Loan createLoanFromApplication(UUID loanApplicationId) {
        LoanApplication application = loanApplicationRepository.findById(loanApplicationId)
            .orElseThrow(() -> new NotFoundException(
                Messages.LOAN_APPLICATION_NOT_FOUND_ID.format(loanApplicationId)));

        if (application.getStatus() != LoanApplicationStatus.APPROVED) {
            throw new IllegalStateException(
                Messages.LOAN_REQUEST_NOT_APPROVED.getValue());
        }

        if (loanRepository.findByLoanApplicationId(loanApplicationId).isPresent()) {
            throw new IllegalStateException(
                Messages.LOAN_ALREADY_EXISTS.getValue());
        }

        // Usamos la tasa snapshot de la solicitud (no la del loan_type actual),
        // garantizando que la amortización refleja las condiciones originales.
        Money monthlyPayment = loanCalculator.calculateMonthlyPayment(
            application.getRequestedAmount().getAmount(),
            application.getAnnualInterestRate(),
            application.getTermInMonths());

        Loan loan = Loan.builder()
                .loanApplication(application)
                .customer(application.getCustomer())
                .principalAmount(application.getRequestedAmount())
                .annualInterestRate(application.getAnnualInterestRate())
                .termInMonths(application.getTermInMonths())
                .monthlyPayment(monthlyPayment)
                .approvedAt(java.time.Instant.now())
                .build();

        Loan saved = loanRepository.save(loan);
        paymentPlanUseCase.generatePaymentPlan(saved.getId());
        return saved;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Loan> findById(UUID id) {
        var opt = loanRepository.findById(id);
        if (opt.isPresent()) return opt;
        return loanRepository.findByLoanApplicationId(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Loan> findByIdForUser(UUID loanId, UUID customerId) {
        var loan = findById(loanId);
        if (loan.isPresent() && !loan.get().getCustomer().getId().equals(customerId)) {
            return Optional.empty();
        }
        return loan;
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<Loan> findAll(int page, int perPage, String sortBy, String sortDir) {
        return loanRepository.findAll(page, perPage, sortBy, sortDir);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<Loan> findByFilters(UUID customerId, String search, int page, int perPage, String sortBy, String sortDir) {
        return loanRepository.findByFilters(customerId, search, page, perPage, sortBy, sortDir);
    }
}
