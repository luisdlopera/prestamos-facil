package com.prestamosfacil.infrastructure.adapter.out.persistence.postgres.customer.repository;

import com.prestamosfacil.infrastructure.adapter.out.persistence.postgres.customer.entity.DocumentTypeEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DocumentTypeJpaRepository extends JpaRepository<DocumentTypeEntity, String> {}
