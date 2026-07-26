package com.prestamosfacil.infrastructure.adapter.in.rest.auth.mapper;

import com.prestamosfacil.domain.customer.models.Customer;
import com.prestamosfacil.domain.user.enums.UserType;
import com.prestamosfacil.domain.user.models.User;
import com.prestamosfacil.infrastructure.adapter.in.rest.auth.dto.response.AuthUserResponse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

@Mapper(componentModel = "spring")
public interface AuthResponseMapper {
    @Mapping(target = "email", source = "email.value")
    @Mapping(target = "documentType", source = "documentNumber.type")
    @Mapping(target = "documentNumber", source = "documentNumber.number")
    @Mapping(target = "baseSalary", source = "baseSalary.amount")
    @Mapping(target = "userType", source = "u", qualifiedByName = "resolveUserType")
    @Mapping(target = "role", source = "role")
    AuthUserResponse toUserFromDomain(User u);

    default AuthUserResponse toUser(Customer c) {
        if (c == null) return null;
        String roleStr = c.getUser() != null && c.getUser().getRole() != null ? c.getUser().getRole() : "CUSTOMER";
        String userType = UserType.CUSTOMER.name().equalsIgnoreCase(roleStr) ? "customer" : "staff";
        return new AuthUserResponse(
            c.getId(),
            c.getFirstName(),
            c.getLastName(),
            c.getEmail() != null ? c.getEmail().getValue() : "",
            c.getDocumentNumber() != null ? c.getDocumentNumber().getType() : "",
            c.getDocumentNumber() != null ? c.getDocumentNumber().getNumber() : "",
            c.getBaseSalary() != null ? c.getBaseSalary().getAmount() : java.math.BigDecimal.ZERO,
            userType,
            roleStr
        );
    }

    default AuthUserResponse toStaffUser(User u) {
        return toUserFromDomain(u);
    }

    @Named("resolveUserType")
    default String resolveUserType(User u) {
        if (u == null || u.getRole() == null) return "customer";
        return UserType.CUSTOMER.name().equalsIgnoreCase(u.getRole()) ? "customer" : "staff";
    }
}
