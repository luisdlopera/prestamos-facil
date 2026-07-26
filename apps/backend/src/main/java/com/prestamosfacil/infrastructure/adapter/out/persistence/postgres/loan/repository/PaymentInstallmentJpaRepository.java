package com.prestamosfacil.infrastructure.adapter.out.persistence.postgres.loan.repository;

import com.prestamosfacil.infrastructure.adapter.out.persistence.postgres.loan.entity.PaymentInstallmentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface PaymentInstallmentJpaRepository extends JpaRepository<PaymentInstallmentEntity, UUID> {
    List<PaymentInstallmentEntity> findByLoanIdOrderByInstallmentNumberAsc(UUID loanId);
}
