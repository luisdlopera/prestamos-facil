package com.prestamosfacil.application.paymentplan;

import com.prestamosfacil.domain.paymentplan.port.in.PaymentPlanUseCase;
import com.prestamosfacil.domain.loan.models.Loan;
import com.prestamosfacil.domain.loan.port.out.LoanRepository;
import com.prestamosfacil.domain.loan.utils.PaymentPlanCalculator;
import com.prestamosfacil.domain.paymentplan.models.PaymentInstallment;
import com.prestamosfacil.domain.paymentplan.port.out.PaymentInstallmentRepository;

import com.prestamosfacil.domain.shared.enums.Messages;
import com.prestamosfacil.domain.shared.exception.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class PaymentPlanUseCaseImpl implements PaymentPlanUseCase {

    private final LoanRepository loanRepository;
    private final PaymentInstallmentRepository installmentRepository;
    private final PaymentPlanCalculator paymentPlanCalculator;

    public PaymentPlanUseCaseImpl(LoanRepository loanRepository,
                                  PaymentInstallmentRepository installmentRepository,
                                  PaymentPlanCalculator paymentPlanCalculator) {
        this.loanRepository = loanRepository;
        this.installmentRepository = installmentRepository;
        this.paymentPlanCalculator = paymentPlanCalculator;
    }

    @Override
    @Transactional
    public List<PaymentInstallment> generatePaymentPlan(UUID loanId) {
        Loan loan = loanRepository.findById(loanId)
            .orElseThrow(() -> new NotFoundException(Messages.LOAN_NOT_FOUND_ID.format(loanId)));

        List<PaymentInstallment> installments = paymentPlanCalculator.calculateInstallments(
            loan.getId(),
            loan.getPrincipalAmount().getAmount(),
            loan.getAnnualInterestRate(),
            loan.getTermInMonths(),
            loan.calculateFirstDueDate());

        return installmentRepository.saveAll(installments);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PaymentInstallment> getPaymentPlan(UUID loanId) {
        var list = installmentRepository.findByLoanId(loanId);
        if (!list.isEmpty()) return list;
        return loanRepository.findByLoanApplicationId(loanId)
            .map(loan -> installmentRepository.findByLoanId(loan.getId()))
            .orElse(List.of());
    }
}
