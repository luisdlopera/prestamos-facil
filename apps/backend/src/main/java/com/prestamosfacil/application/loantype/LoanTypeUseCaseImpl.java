package com.prestamosfacil.application.loantype;

import com.prestamosfacil.domain.loantype.models.LoanType;
import com.prestamosfacil.domain.loantype.port.in.LoanTypeUseCase;
import com.prestamosfacil.domain.loantype.port.out.LoanTypeRepository;
import com.prestamosfacil.domain.shared.PageResult;
import com.prestamosfacil.domain.shared.enums.Messages;
import com.prestamosfacil.domain.shared.exception.ApplicationException;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class LoanTypeUseCaseImpl implements LoanTypeUseCase {

    private final LoanTypeRepository loanTypeRepository;

    public LoanTypeUseCaseImpl(LoanTypeRepository loanTypeRepository) {
        this.loanTypeRepository = loanTypeRepository;
    }

    @Override
    public List<LoanType> findAllActive() {
        return loanTypeRepository.findAllActive();
    }

    @Override
    public PageResult<LoanType> findAllAdmin(String search, Boolean active, int page, int size, String sortBy, String sortDir) {
        int validatedPage = Math.max(0, page);
        int validatedSize = size > 0 ? Math.min(size, 100) : 10;
        return loanTypeRepository.findAllAdmin(search, active, validatedPage, validatedSize, sortBy, sortDir);
    }

    @Override
    public Optional<LoanType> findById(UUID id) {
        return loanTypeRepository.findById(id);
    }

    @Override
    @Transactional
    public LoanType create(LoanType loanType) {
        if (loanType == null) {
            throw new ApplicationException(Messages.LOAN_TYPE_DATA_REQUIRED);
        }
        loanType.validate();

        if (loanTypeRepository.existsByName(loanType.getName(), null)) {
            throw new ApplicationException(Messages.LOAN_TYPE_NAME_EXISTS.format(loanType.getName()));
        }

        if (loanType.getId() == null) {
            loanType.setId(UUID.randomUUID());
        }
        Instant now = Instant.now();
        if (loanType.getCreatedAt() == null) {
            loanType.setCreatedAt(now);
        }
        loanType.setUpdatedAt(now);

        return loanTypeRepository.save(loanType);
    }

    @Override
    @Transactional
    public LoanType update(UUID id, LoanType updatedData) {
        if (id == null || updatedData == null) {
            throw new ApplicationException(Messages.LOAN_TYPE_ID_AND_DATA_REQUIRED);
        }
        updatedData.validate();

        LoanType existing = loanTypeRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(Messages.LOAN_TYPE_NOT_FOUND_CREDIT_ID));

        if (loanTypeRepository.existsByName(updatedData.getName(), id)) {
            throw new ApplicationException(Messages.LOAN_TYPE_NAME_EXISTS.format(updatedData.getName()));
        }

        existing.setName(updatedData.getName());
        existing.setDescription(updatedData.getDescription());
        existing.setInterestRate(updatedData.getInterestRate());
        existing.setRateType(updatedData.getRateType());
        existing.setMinAmount(updatedData.getMinAmount());
        existing.setMaxAmount(updatedData.getMaxAmount());
        existing.setMinTermMonths(updatedData.getMinTermMonths());
        existing.setMaxTermMonths(updatedData.getMaxTermMonths());
        existing.setActive(updatedData.isActive());
        existing.setDisplayOrder(updatedData.getDisplayOrder());
        existing.setAutomaticValidationEnabled(updatedData.isAutomaticValidationEnabled());
        existing.setUpdatedAt(Instant.now());

        return loanTypeRepository.save(existing);
    }

    @Override
    @Transactional
    public LoanType toggleStatus(UUID id, boolean active) {
        LoanType existing = loanTypeRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(Messages.LOAN_TYPE_NOT_FOUND_CREDIT));

        existing.setActive(active);
        existing.setUpdatedAt(Instant.now());
        return loanTypeRepository.save(existing);
    }

    @Override
    @Transactional
    public void reorder(List<UUID> orderedIds) {
        if (orderedIds == null || orderedIds.isEmpty()) {
            return;
        }

        List<LoanType> loanTypes = loanTypeRepository.findAllById(orderedIds);
        List<LoanType> updatedList = new ArrayList<>();

        for (int order = 0; order < orderedIds.size(); order++) {
            UUID id = orderedIds.get(order);
            final int currentOrder = order + 1;
            loanTypes.stream()
                    .filter(lt -> lt.getId().equals(id))
                    .findFirst()
                    .ifPresent(lt -> {
                        lt.setDisplayOrder(currentOrder);
                        lt.setUpdatedAt(Instant.now());
                        updatedList.add(lt);
                    });
        }

        if (!updatedList.isEmpty()) {
            loanTypeRepository.saveAll(updatedList);
        }
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        LoanType existing = loanTypeRepository.findById(id)
                .orElseThrow(() -> new ApplicationException(Messages.LOAN_TYPE_NOT_FOUND_CREDIT));

        if (loanTypeRepository.hasRelatedRecords(id)) {
            throw new ApplicationException(
                Messages.LOAN_TYPE_DELETE_HAS_RELATIONS.format(existing.getName())
            );
        }

        loanTypeRepository.deleteById(id);
    }
}
