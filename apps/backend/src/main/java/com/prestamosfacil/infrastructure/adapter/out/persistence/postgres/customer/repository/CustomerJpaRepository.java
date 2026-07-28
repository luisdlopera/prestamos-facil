package com.prestamosfacil.infrastructure.adapter.out.persistence.postgres.customer.repository;

import com.prestamosfacil.infrastructure.adapter.out.persistence.postgres.customer.entity.CustomerEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
import java.util.UUID;

public interface CustomerJpaRepository extends JpaRepository<CustomerEntity, UUID> {
    @Query("SELECT c FROM CustomerEntity c JOIN FETCH c.user u WHERE u.id = :userId")
    Optional<CustomerEntity> findByUserId(@Param("userId") UUID userId);
    @Query("SELECT c FROM CustomerEntity c JOIN c.user u WHERE LOWER(u.email) = LOWER(:email)")
    Optional<CustomerEntity> findByEmail(@Param("email") String email);
    boolean existsByDocumentNumber(String documentNumber);

    @Query("SELECT c FROM CustomerEntity c JOIN c.user u WHERE UPPER(u.role) = 'CUSTOMER'")
    Page<CustomerEntity> findAllCustomers(Pageable pageable);

    @Query("SELECT c FROM CustomerEntity c JOIN c.user u WHERE UPPER(u.role) = 'CUSTOMER' AND (LOWER(c.firstName) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(c.lastName) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(u.email) LIKE LOWER(CONCAT('%', :search, '%')) OR c.documentNumber LIKE CONCAT('%', :search, '%'))")
    Page<CustomerEntity> findBySearch(@Param("search") String search, Pageable pageable);
}
