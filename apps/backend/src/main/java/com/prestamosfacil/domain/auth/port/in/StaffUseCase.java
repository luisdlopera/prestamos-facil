package com.prestamosfacil.domain.auth.port.in;

import com.prestamosfacil.domain.user.models.User;

import java.util.Optional;
import java.util.UUID;

public interface StaffUseCase {
    Optional<User> findById(UUID id);
    User registerStaff(String name, String email, String password, String role);
    User updateStaffProfile(UUID id, String firstName, String lastName);
}
