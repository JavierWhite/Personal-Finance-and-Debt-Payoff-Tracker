package com.javier.finance.savings.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.javier.finance.savings.dto.SavingsContributionRequest;
import com.javier.finance.savings.dto.SavingsGoalRequest;
import com.javier.finance.savings.entity.SavingsContribution;
import com.javier.finance.savings.entity.SavingsGoal;
import com.javier.finance.savings.entity.SavingsStatus;
import com.javier.finance.savings.exception.ResourceNotFoundException;
import com.javier.finance.savings.repository.SavingsContributionRepository;
import com.javier.finance.savings.repository.SavingsGoalRepository;

@Service
@Transactional
public class SavingsGoalService {
    private final SavingsGoalRepository goalRepository;
    private final SavingsContributionRepository contributionRepository;

    public SavingsGoalService(SavingsGoalRepository goalRepository,
            SavingsContributionRepository contributionRepository) {
        this.goalRepository = goalRepository;
        this.contributionRepository = contributionRepository;
    }

    public SavingsGoal create(SavingsGoalRequest request) {
        SavingsGoal goal = new SavingsGoal();
        apply(goal, request, true);
        return goalRepository.save(goal);
    }

    @Transactional(readOnly = true)
    public List<SavingsGoal> findAll() { return goalRepository.findAll(); }

    @Transactional(readOnly = true)
    public List<SavingsGoal> findByUserId(Long userId) {
        return goalRepository.findByUserIdOrderByCreatedDateDesc(userId);
    }

    @Transactional(readOnly = true)
    public SavingsGoal findById(Long id) {
        return goalRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Savings goal " + id + " was not found"));
    }

    public SavingsGoal update(Long id, SavingsGoalRequest request) {
        SavingsGoal goal = findById(id);
        apply(goal, request, false);
        return goalRepository.save(goal);
    }

    public void delete(Long id) { goalRepository.delete(findById(id)); }

    public SavingsGoal addContribution(Long goalId, SavingsContributionRequest request) {
        SavingsGoal goal = findById(goalId);
        if (goal.getStatus() == SavingsStatus.CANCELLED) {
            throw new IllegalArgumentException("Contributions cannot be added to a cancelled goal");
        }
        SavingsContribution contribution = new SavingsContribution();
        contribution.setAmount(request.amount());
        contribution.setContributionDate(request.contributionDate() == null ? LocalDate.now() : request.contributionDate());
        contribution.setNote(request.note());
        contribution.setSavingsGoal(goal);
        contributionRepository.save(contribution);
        goal.getContributions().add(contribution);

        BigDecimal newAmount = goal.getCurrentAmount().add(request.amount());
        goal.setCurrentAmount(newAmount);
        if (newAmount.compareTo(goal.getTargetAmount()) >= 0) {
            goal.setStatus(SavingsStatus.COMPLETED);
        }
        return goalRepository.save(goal);
    }

    private void apply(SavingsGoal goal, SavingsGoalRequest request, boolean creating) {
        goal.setUserId(request.userId());
        goal.setGoalName(request.goalName());
        goal.setTargetAmount(request.targetAmount());
        if (creating) {
            goal.setCurrentAmount(request.currentAmount() == null ? BigDecimal.ZERO : request.currentAmount());
        } else if (request.currentAmount() != null) {
            goal.setCurrentAmount(request.currentAmount());
        }
        goal.setTargetDate(request.targetDate());
        if (request.status() != null) goal.setStatus(request.status());
    }
}
