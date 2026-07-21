package com.javier.finance.user.service;

import java.util.List;

import com.javier.finance.security.InputSecurityValidator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.javier.finance.user.dto.IncomeRecordRequest;
import com.javier.finance.user.entity.IncomeRecord;
import com.javier.finance.user.exception.ResourceNotFoundException;
import com.javier.finance.user.repository.IncomeRecordRepository;
import com.javier.finance.user.repository.UserAccountRepository;

@Service
@Transactional
public class IncomeRecordService {
    private final IncomeRecordRepository incomeRepository;
    private final UserAccountRepository userRepository;

    public IncomeRecordService(IncomeRecordRepository incomeRepository, UserAccountRepository userRepository) {
        this.incomeRepository = incomeRepository;
        this.userRepository = userRepository;
    }

    public IncomeRecord create(IncomeRecordRequest request) {
        ensureUserExists(request.userId());
        IncomeRecord income = new IncomeRecord();
        apply(income, request);
        return incomeRepository.save(income);
    }

    @Transactional(readOnly = true)
    public List<IncomeRecord> findAll() {
        return incomeRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<IncomeRecord> findByUserId(Long userId) {
        ensureUserExists(userId);
        return incomeRepository.findByUserIdOrderByCreatedDateDesc(userId);
    }

    @Transactional(readOnly = true)
    public IncomeRecord findById(Long id) {
        return incomeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Income record " + id + " was not found"));
    }

    public IncomeRecord update(Long id, IncomeRecordRequest request) {
        ensureUserExists(request.userId());
        IncomeRecord income = findById(id);
        apply(income, request);
        return incomeRepository.save(income);
    }

    public void delete(Long id) {
        incomeRepository.delete(findById(id));
    }

    private void ensureUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User account " + userId + " was not found");
        }
    }

    private void apply(IncomeRecord income, IncomeRecordRequest request) {
        income.setUserId(request.userId());
        income.setSource(InputSecurityValidator.safeText("income source", request.source(), 100));
        income.setGrossAmount(request.grossAmount());
        income.setFrequency(request.frequency());
        income.setEffectiveDate(request.effectiveDate());
        income.setActive(request.active() == null || request.active());
    }
}
