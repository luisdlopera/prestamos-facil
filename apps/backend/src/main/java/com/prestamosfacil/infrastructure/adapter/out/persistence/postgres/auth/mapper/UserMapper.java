package com.prestamosfacil.infrastructure.adapter.out.persistence.postgres.auth.mapper;

import com.prestamosfacil.domain.shared.EmailAddress;
import com.prestamosfacil.domain.user.models.User;
import com.prestamosfacil.infrastructure.adapter.out.persistence.postgres.auth.entity.UserEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "email", source = "email", qualifiedByName = "emailToValueObject")
    @Mapping(target = "firstName", ignore = true)
    @Mapping(target = "lastName", ignore = true)
    @Mapping(target = "documentNumber", ignore = true)
    @Mapping(target = "baseSalary", ignore = true)
    User toDomain(UserEntity entity);

    @Mapping(target = "email", source = "email", qualifiedByName = "emailToString")
    @Mapping(target = "id", source = "id")
    UserEntity toEntity(User domain);

    @org.mapstruct.Named("emailToValueObject")
    default EmailAddress emailToValueObject(String value) { return value != null ? new EmailAddress(value) : null; }
    @org.mapstruct.Named("emailToString")
    default String emailToString(EmailAddress value) { return value != null ? value.getValue() : null; }
}
