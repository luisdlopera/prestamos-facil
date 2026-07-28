package com.prestamosfacil.application.loantype;

import com.prestamosfacil.domain.loantype.models.LoanType;
import com.prestamosfacil.domain.loantype.enums.RateType;
import com.prestamosfacil.domain.loantype.port.out.LoanTypeRepository;
import com.prestamosfacil.domain.shared.Money;
import com.prestamosfacil.domain.shared.PageResult;
import com.prestamosfacil.domain.shared.exception.ApplicationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class LoanTypeUseCaseImplTest {

    private LoanTypeRepository loanTypeRepository;
    private LoanTypeUseCaseImpl loanTypeUseCase;

    @BeforeEach
    void setUp() {
        loanTypeRepository = mock(LoanTypeRepository.class);
        loanTypeUseCase = new LoanTypeUseCaseImpl(loanTypeRepository);
    }

    @Test
    void shouldFindAllActive() {
        List<LoanType> types = List.of(
            new LoanType("Personal", new BigDecimal("12.0"),
                new Money(new BigDecimal("1000000")), new Money(new BigDecimal("50000000"))));
        when(loanTypeRepository.findAllActive()).thenReturn(types);

        List<LoanType> result = loanTypeUseCase.findAllActive();
        assertEquals(1, result.size());
        verify(loanTypeRepository).findAllActive();
    }

    @Test
    void shouldFindAllAdmin() {
        List<LoanType> types = List.of(
            new LoanType("Personal", new BigDecimal("12.0"),
                new Money(new BigDecimal("1000000")), new Money(new BigDecimal("50000000"))));
        PageResult<LoanType> pageResult = new PageResult<>(types, 0, 10, 1);
        when(loanTypeRepository.findAllAdmin("Personal", true, 0, 10, null, null)).thenReturn(pageResult);

        PageResult<LoanType> result = loanTypeUseCase.findAllAdmin("Personal", true, 0, 10, null, null);
        assertEquals(1, result.getContent().size());
        assertEquals(1, result.getTotal());
    }

    @Test
    void shouldCreateLoanTypeSuccessfully() {
        LoanType input = LoanType.builder()
            .name("Educativo Especial")
            .description("Para posgrados")
            .interestRate(new BigDecimal("8.50"))
            .rateType(RateType.EA)
            .minAmount(new Money(new BigDecimal("1000000")))
            .maxAmount(new Money(new BigDecimal("20000000")))
            .minTermMonths(6)
            .maxTermMonths(36)
            .displayOrder(1)
            .active(true)
            .build();

        when(loanTypeRepository.existsByName("Educativo Especial", null)).thenReturn(false);
        when(loanTypeRepository.save(any(LoanType.class))).thenAnswer(i -> i.getArgument(0));

        LoanType created = loanTypeUseCase.create(input);

        assertNotNull(created.getId());
        assertEquals("Educativo Especial", created.getName());
        verify(loanTypeRepository).save(any(LoanType.class));
    }

    @Test
    void shouldFailCreateWhenNameAlreadyExists() {
        LoanType input = LoanType.builder()
            .name("Libre Inversión")
            .interestRate(new BigDecimal("18.50"))
            .minAmount(new Money(new BigDecimal("1000000")))
            .maxAmount(new Money(new BigDecimal("50000000")))
            .minTermMonths(6)
            .maxTermMonths(60)
            .build();

        when(loanTypeRepository.existsByName("Libre Inversión", null)).thenReturn(true);

        assertThrows(ApplicationException.class, () -> loanTypeUseCase.create(input));
        verify(loanTypeRepository, never()).save(any());
    }

    @Test
    void shouldFailCreateWhenMaxAmountIsLessThanMinAmount() {
        LoanType input = LoanType.builder()
            .name("Invalido")
            .interestRate(new BigDecimal("10.00"))
            .minAmount(new Money(new BigDecimal("5000000")))
            .maxAmount(new Money(new BigDecimal("1000000"))) // invalid
            .minTermMonths(6)
            .maxTermMonths(24)
            .build();

        assertThrows(IllegalArgumentException.class, () -> loanTypeUseCase.create(input));
    }

    @Test
    void shouldUpdateLoanTypeSuccessfully() {
        UUID id = UUID.randomUUID();
        LoanType existing = new LoanType("Antiguo", new BigDecimal("12.00"),
            new Money(new BigDecimal("1000000")), new Money(new BigDecimal("10000000")));
        existing.setId(id);

        LoanType updateData = LoanType.builder()
            .id(id)
            .name("Nuevo Nombre")
            .description("Nueva desc")
            .interestRate(new BigDecimal("14.00"))
            .rateType(RateType.EA)
            .minAmount(new Money(new BigDecimal("2000000")))
            .maxAmount(new Money(new BigDecimal("15000000")))
            .minTermMonths(12)
            .maxTermMonths(48)
            .active(true)
            .displayOrder(2)
            .build();

        when(loanTypeRepository.findById(id)).thenReturn(Optional.of(existing));
        when(loanTypeRepository.existsByName("Nuevo Nombre", id)).thenReturn(false);
        when(loanTypeRepository.save(any(LoanType.class))).thenAnswer(i -> i.getArgument(0));

        LoanType updated = loanTypeUseCase.update(id, updateData);

        assertEquals("Nuevo Nombre", updated.getName());
        assertEquals(new BigDecimal("14.00"), updated.getInterestRate());
        assertEquals(RateType.EA, updated.getRateType());
    }

    @Test
    void shouldToggleStatus() {
        UUID id = UUID.randomUUID();
        LoanType existing = new LoanType("Test", new BigDecimal("12.00"),
            new Money(new BigDecimal("1000000")), new Money(new BigDecimal("10000000")));
        existing.setId(id);
        existing.setActive(true);

        when(loanTypeRepository.findById(id)).thenReturn(Optional.of(existing));
        when(loanTypeRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        LoanType toggled = loanTypeUseCase.toggleStatus(id, false);
        assertFalse(toggled.isActive());
    }

    @Test
    void shouldReorderLoanTypes() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        LoanType lt1 = new LoanType("T1", new BigDecimal("10.00"), new Money(new BigDecimal("100")), new Money(new BigDecimal("1000")));
        lt1.setId(id1);
        LoanType lt2 = new LoanType("T2", new BigDecimal("12.00"), new Money(new BigDecimal("200")), new Money(new BigDecimal("2000")));
        lt2.setId(id2);

        when(loanTypeRepository.findAllById(List.of(id2, id1))).thenReturn(List.of(lt1, lt2));

        loanTypeUseCase.reorder(List.of(id2, id1));

        verify(loanTypeRepository).saveAll(any());
        assertEquals(2, lt1.getDisplayOrder());
        assertEquals(1, lt2.getDisplayOrder());
    }

    @Test
    void shouldDeleteWhenNoRelatedRecords() {
        UUID id = UUID.randomUUID();
        LoanType lt = new LoanType("Borrar", new BigDecimal("10.00"), new Money(new BigDecimal("100")), new Money(new BigDecimal("1000")));
        lt.setId(id);

        when(loanTypeRepository.findById(id)).thenReturn(Optional.of(lt));
        when(loanTypeRepository.hasRelatedRecords(id)).thenReturn(false);

        loanTypeUseCase.delete(id);

        verify(loanTypeRepository).deleteById(id);
    }

    @Test
    void shouldFailDeleteWhenRelatedRecordsExist() {
        UUID id = UUID.randomUUID();
        LoanType lt = new LoanType("En Uso", new BigDecimal("10.00"), new Money(new BigDecimal("100")), new Money(new BigDecimal("1000")));
        lt.setId(id);

        when(loanTypeRepository.findById(id)).thenReturn(Optional.of(lt));
        when(loanTypeRepository.hasRelatedRecords(id)).thenReturn(true);

        assertThrows(ApplicationException.class, () -> loanTypeUseCase.delete(id));
        verify(loanTypeRepository, never()).deleteById(id);
    }
}
