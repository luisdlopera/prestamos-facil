package com.prestamosfacil.domain.user.port.out;

import com.prestamosfacil.domain.user.models.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository {
    Optional<User> findById(UUID id);
    Optional<User> findByEmail(String email);
    default Optional<User> findByDocumentNumber(String documentNumber) { return Optional.empty(); }
    User save(User user);
}
