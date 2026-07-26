package com.prestamosfacil.infrastructure.adapter.out.persistence.postgres.loanapplication.repository;

import com.prestamosfacil.infrastructure.adapter.out.persistence.postgres.loanapplication.entity.LoanApplicationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.UUID;

public interface LoanApplicationJpaRepository extends JpaRepository<LoanApplicationEntity, UUID>, JpaSpecificationExecutor<LoanApplicationEntity> {}
