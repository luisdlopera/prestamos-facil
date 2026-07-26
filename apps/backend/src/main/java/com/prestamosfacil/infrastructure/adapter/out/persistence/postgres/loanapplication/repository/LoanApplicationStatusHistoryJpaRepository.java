package com.prestamosfacil.infrastructure.adapter.out.persistence.postgres.loanapplication.repository;

import com.prestamosfacil.infrastructure.adapter.out.persistence.postgres.loanapplication.entity.LoanApplicationStatusHistoryEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Repository
public interface LoanApplicationStatusHistoryJpaRepository extends JpaRepository<LoanApplicationStatusHistoryEntity, UUID> {
    Optional<LoanApplicationStatusHistoryEntity> findByLoanApplicationIdAndClosedAtIsNull(UUID loanApplicationId);
    List<LoanApplicationStatusHistoryEntity> findByLoanApplicationIdInAndClosedAtIsNull(Set<UUID> loanApplicationIds);
    List<LoanApplicationStatusHistoryEntity> findByLoanApplicationIdOrderByOpenedAtDesc(UUID loanApplicationId);
}
