package com.prestamosfacil.infrastructure.adapter.out.persistence.postgres.loan.repository;

import com.prestamosfacil.infrastructure.adapter.out.persistence.postgres.loan.entity.LoanEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LoanJpaRepository extends JpaRepository<LoanEntity, UUID>, JpaSpecificationExecutor<LoanEntity> {
    Optional<LoanEntity> findByLoanApplicationId(UUID loanApplicationId);
    Optional<LoanEntity> findByIdAndCustomerId(UUID id, UUID customerId);
    @Query("SELECT COUNT(l), COALESCE(SUM(l.principalAmount), 0) FROM LoanEntity l WHERE l.approvedAt IS NOT NULL AND l.status = 'APPROVED'")
    List<Object[]> getTotalApprovedAggregation();
    @Query("SELECT COUNT(l), COALESCE(SUM(l.principalAmount), 0), COALESCE(AVG(l.annualInterestRate), 0) FROM LoanEntity l WHERE l.status = 'APPROVED'")
    List<Object[]> getLoanStatisticsAggregation();
}
