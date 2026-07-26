package com.prestamosfacil.infrastructure.adapter.out.persistence.postgres.loanapplication.repository;

import com.prestamosfacil.infrastructure.adapter.out.persistence.postgres.loanapplication.entity.LoanApplicationStatusEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LoanApplicationStatusJpaRepository extends JpaRepository<LoanApplicationStatusEntity, String> {}
