package com.prestamosfacil.application.customer;

import com.prestamosfacil.domain.shared.exception.ApplicationException;
import com.prestamosfacil.domain.customer.models.Customer;
import com.prestamosfacil.domain.customer.models.DocumentNumber;
import com.prestamosfacil.domain.customer.port.in.CustomerUseCase;
import com.prestamosfacil.domain.customer.port.out.CustomerRepository;
import com.prestamosfacil.domain.shared.EmailAddress;
import com.prestamosfacil.domain.shared.Money;
import com.prestamosfacil.domain.shared.PageResult;
import com.prestamosfacil.domain.shared.enums.Messages;
import com.prestamosfacil.domain.user.models.User;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

@Service
public class CustomerUseCaseImpl implements CustomerUseCase {

    private final CustomerRepository customerRepository;

    public CustomerUseCaseImpl(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Override
    @Transactional
    public Customer createCustomer(String firstName, String lastName, String email,
                                    String documentType, String documentNumber,
                                    BigDecimal baseSalary) {
        EmailAddress emailVo = new EmailAddress(email);
        DocumentNumber documentVo = new DocumentNumber(documentType, documentNumber);
        Money salaryVo = new Money(baseSalary);

        if (customerRepository.existsByEmail(emailVo.getValue())) {
            throw new ApplicationException(Messages.AUTH_EMAIL_EXISTS.format(emailVo.getValue()));
        }
        if (customerRepository.existsByDocumentNumber(documentVo.getNumber())) {
            throw new ApplicationException(Messages.AUTH_DOCUMENT_EXISTS.format(documentVo.getNumber()));
        }

        User user = User.builder()
                .email(emailVo)
                .build();

        Customer customer = Customer.builder()
                .user(user)
                .firstName(firstName)
                .lastName(lastName)
                .email(emailVo)
                .documentNumber(documentVo)
                .baseSalary(salaryVo)
                .build();
        return customerRepository.save(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Customer> findById(UUID id) {
        return customerRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<Customer> findAll(int page, int perPage, String sortBy, String sortDir) {
        return customerRepository.findAll(page, perPage, sortBy, sortDir);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResult<Customer> findBySearch(String search, int page, int perPage, String sortBy, String sortDir) {
        return customerRepository.findBySearch(search, page, perPage, sortBy, sortDir);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Customer> findByUserId(UUID userId) {
        return customerRepository.findByUserId(userId);
    }
}
