package com.prestamosfacil.infrastructure.adapter.out.persistence.postgres.loantype.repository;

import com.prestamosfacil.infrastructure.adapter.out.persistence.postgres.loantype.entity.LoanTypeEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface LoanTypeJpaRepository extends JpaRepository<LoanTypeEntity, UUID> {
    List<LoanTypeEntity> findByActiveTrueOrderByDisplayOrderAscNameAsc();
    Page<LoanTypeEntity> findByActive(boolean active, Pageable pageable);
    Page<LoanTypeEntity> findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(String name, String desc, Pageable p);
    Page<LoanTypeEntity> findByNameContainingIgnoreCaseAndActiveOrDescriptionContainingIgnoreCaseAndActive(String n, boolean a1, String d, boolean a2, Pageable p);
    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndIdNot(String name, UUID id);
    @Query(value = "SELECT COUNT(*) FROM loan_applications WHERE loan_type_id = :id", nativeQuery = true)
    long countRelatedApplications(@Param("id") UUID id);
}
