package com.prestamosfacil.infrastructure.adapter.out.persistence.postgres.shared;

import com.prestamosfacil.infrastructure.adapter.out.persistence.postgres.auth.entity.UserEntity;
import com.prestamosfacil.infrastructure.adapter.out.persistence.postgres.customer.entity.CustomerEntity;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Subquery;

import java.util.UUID;

public final class SearchSpecUtils {

    private SearchSpecUtils() {}

    public static Subquery<UUID> buildCustomerSearchSubquery(CriteriaQuery<?> query, CriteriaBuilder cb, String searchPattern) {
        var sub = query.subquery(UUID.class);
        var cr = sub.from(CustomerEntity.class);
        Join<CustomerEntity, UserEntity> userJoin = cr.join("user");
        sub.select(cr.get("id")).where(cb.or(
            cb.like(cb.lower(cr.get("firstName")), searchPattern),
            cb.like(cb.lower(cr.get("lastName")), searchPattern),
            cb.like(cb.lower(userJoin.get("email")), searchPattern),
            cb.like(cb.lower(cr.get("documentNumber")), searchPattern),
            cb.like(cb.lower(cb.concat(cb.concat(cr.get("firstName"), " "), cr.get("lastName"))), searchPattern)
        ));
        return sub;
    }
}
