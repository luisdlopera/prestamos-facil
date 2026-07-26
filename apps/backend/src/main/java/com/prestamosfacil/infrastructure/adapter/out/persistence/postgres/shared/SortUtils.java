package com.prestamosfacil.infrastructure.adapter.out.persistence.postgres.shared;

import org.springframework.data.domain.Sort;

import java.util.Set;

public final class SortUtils {

    private SortUtils() {}

    public static Sort buildSort(Set<String> allowedFields, String sortBy, String sortDir) {
        return buildSort(allowedFields, sortBy, sortDir, Sort.by(Sort.Direction.DESC, "createdAt"));
    }

    public static Sort buildSort(Set<String> allowedFields, String sortBy, String sortDir, Sort defaultSort) {
        if (sortBy != null && !sortBy.isBlank() && allowedFields.contains(sortBy)) {
            return Sort.by("asc".equalsIgnoreCase(sortDir) ? Sort.Direction.ASC : Sort.Direction.DESC, sortBy);
        }
        return defaultSort;
    }
}
