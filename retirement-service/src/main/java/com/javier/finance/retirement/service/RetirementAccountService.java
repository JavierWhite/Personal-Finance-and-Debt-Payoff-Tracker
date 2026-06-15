package com.javier.finance.retirement.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.javier.finance.retirement.dto.RetirementAccountRequest;
import com.javier.finance.retirement.dto.RetirementContributionRequest;
import com.javier.finance.retirement.dto.RetirementProjectionResponse;
import com.javier.finance.retirement.entity.RetirementAccount;
import com.javier.finance.retirement.entity.RetirementContribution;
import com.javier.finance.retirement.exception.ResourceNotFoundException;
import com.javier.finance.retirement.repository.RetirementAccountRepository;
import com.javier.finance.retirement.repository.RetirementContributionRepository;

@Service
@Transactional
public class RetirementAccountService {
    private final RetirementAccountRepository accountRepository;
    private final RetirementContributionRepository contributionRepository;

    public RetirementAccountService(RetirementAccountRepository accountRepository,
            RetirementContributionRepository contributionRepository) {
        this.accountRepository = accountRepository;
        this.contributionRepository = contributionRepository;
    }

    public RetirementAccount create(RetirementAccountRequest request) {
        validateAges(request.currentAge(), request.targetRetirementAge());
        RetirementAccount account = new RetirementAccount();
        apply(account, request);
        return accountRepository.save(account);
    }

    @Transactional(readOnly = true)
    public List<RetirementAccount> findAll() { return accountRepository.findAll(); }

    @Transactional(readOnly = true)
    public List<RetirementAccount> findByUserId(Long userId) {
        return accountRepository.findByUserIdOrderByCreatedDateDesc(userId);
    }

    @Transactional(readOnly = true)
    public RetirementAccount findById(Long id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Retirement account " + id + " was not found"));
    }

    public RetirementAccount update(Long id, RetirementAccountRequest request) {
        validateAges(request.currentAge(), request.targetRetirementAge());
        RetirementAccount account = findById(id);
        apply(account, request);
        return accountRepository.save(account);
    }

    public void delete(Long id) { accountRepository.delete(findById(id)); }

    public RetirementAccount addContribution(Long accountId, RetirementContributionRequest request) {
        RetirementAccount account = findById(accountId);
        RetirementContribution contribution = new RetirementContribution();
        contribution.setAmount(request.amount());
        contribution.setContributionDate(request.contributionDate() == null ? LocalDate.now() : request.contributionDate());
        contribution.setNote(request.note());
        contribution.setRetirementAccount(account);
        contributionRepository.save(contribution);
        account.getContributions().add(contribution);
        account.setCurrentBalance(account.getCurrentBalance().add(request.amount()));
        return accountRepository.save(account);
    }

    @Transactional(readOnly = true)
    public RetirementProjectionResponse project(Long id) {
        RetirementAccount account = findById(id);
        int years = account.getTargetRetirementAge() - account.getCurrentAge();
        int months = years * 12;
        double monthlyRate = account.getExpectedAnnualReturn().doubleValue() / 100.0 / 12.0;
        double growthFactor = Math.pow(1.0 + monthlyRate, months);
        double projectedCurrent = account.getCurrentBalance().doubleValue() * growthFactor;
        double projectedContributions = monthlyRate == 0.0
                ? account.getMonthlyContribution().doubleValue() * months
                : account.getMonthlyContribution().doubleValue() * ((growthFactor - 1.0) / monthlyRate);
        BigDecimal projected = BigDecimal.valueOf(projectedCurrent + projectedContributions)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal difference = projected.subtract(account.getTargetRetirementAmount())
                .setScale(2, RoundingMode.HALF_UP);

        return new RetirementProjectionResponse(account.getId(), account.getCurrentAge(),
                account.getTargetRetirementAge(), years, account.getCurrentBalance(),
                account.getMonthlyContribution(), account.getExpectedAnnualReturn(), projected,
                account.getTargetRetirementAmount(), difference);
    }

    private void validateAges(Integer currentAge, Integer targetAge) {
        if (targetAge <= currentAge) {
            throw new IllegalArgumentException("Target retirement age must be greater than current age");
        }
    }

    private void apply(RetirementAccount account, RetirementAccountRequest request) {
        account.setUserId(request.userId());
        account.setAccountName(request.accountName());
        account.setAccountType(request.accountType());
        account.setCurrentBalance(request.currentBalance());
        account.setMonthlyContribution(request.monthlyContribution());
        account.setExpectedAnnualReturn(request.expectedAnnualReturn());
        account.setTargetRetirementAmount(request.targetRetirementAmount());
        account.setCurrentAge(request.currentAge());
        account.setTargetRetirementAge(request.targetRetirementAge());
    }
}
