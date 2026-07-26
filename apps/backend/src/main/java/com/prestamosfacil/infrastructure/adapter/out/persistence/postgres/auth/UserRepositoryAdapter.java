package com.prestamosfacil.infrastructure.adapter.out.persistence.postgres.auth;

import com.prestamosfacil.domain.user.models.User;
import com.prestamosfacil.domain.user.port.out.UserRepository;
import com.prestamosfacil.infrastructure.adapter.out.persistence.postgres.auth.entity.UserEntity;
import com.prestamosfacil.infrastructure.adapter.out.persistence.postgres.auth.mapper.UserMapper;
import com.prestamosfacil.infrastructure.adapter.out.persistence.postgres.auth.repository.UserJpaRepository;
import org.springframework.stereotype.Component;
import java.util.Optional;
import java.util.UUID;

@Component
public class UserRepositoryAdapter implements UserRepository {
    private final UserJpaRepository repository;
    private final UserMapper mapper;

    public UserRepositoryAdapter(UserJpaRepository repository, UserMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }
    @Override public Optional<User> findById(UUID id) { return repository.findById(id).map(mapper::toDomain); }
    @Override public Optional<User> findByEmail(String email) { return repository.findByEmail(email).map(mapper::toDomain); }
    @Override public User save(User user) { return mapper.toDomain(repository.save(mapper.toEntity(user))); }
}
