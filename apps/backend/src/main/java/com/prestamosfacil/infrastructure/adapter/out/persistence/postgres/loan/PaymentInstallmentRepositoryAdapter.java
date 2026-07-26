package com.prestamosfacil.infrastructure.adapter.out.persistence.postgres.loan;

import com.prestamosfacil.domain.paymentplan.models.PaymentInstallment;
import com.prestamosfacil.domain.paymentplan.port.out.PaymentInstallmentRepository;
import com.prestamosfacil.infrastructure.adapter.out.persistence.postgres.loan.entity.PaymentInstallmentEntity;
import com.prestamosfacil.infrastructure.adapter.out.persistence.postgres.loan.mapper.PaymentInstallmentMapper;
import com.prestamosfacil.infrastructure.adapter.out.persistence.postgres.loan.repository.PaymentInstallmentJpaRepository;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.UUID;

@Component
public class PaymentInstallmentRepositoryAdapter implements PaymentInstallmentRepository {
    private final PaymentInstallmentJpaRepository jpaRepository;
    private final PaymentInstallmentMapper mapper;
    public PaymentInstallmentRepositoryAdapter(PaymentInstallmentJpaRepository jpaRepository, PaymentInstallmentMapper mapper) {
        this.jpaRepository = jpaRepository; this.mapper = mapper;
    }
    @Override public List<PaymentInstallment> findByLoanId(UUID loanId) { return jpaRepository.findByLoanIdOrderByInstallmentNumberAsc(loanId).stream().map(mapper::toDomain).toList(); }
    @Override public PaymentInstallment save(PaymentInstallment pi) { return mapper.toDomain(jpaRepository.save(mapper.toEntity(pi))); }
    @Override public List<PaymentInstallment> saveAll(List<PaymentInstallment> installments) { return jpaRepository.saveAll(installments.stream().map(mapper::toEntity).toList()).stream().map(mapper::toDomain).toList(); }
}
