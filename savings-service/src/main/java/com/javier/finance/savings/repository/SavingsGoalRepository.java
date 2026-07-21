package com.javier.finance.savings.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.javier.finance.savings.entity.SavingsGoal;

public interface SavingsGoalRepository extends JpaRepository<SavingsGoal, Long> {
    List<SavingsGoal> findByUserIdOrderByCreatedDateDesc(Long userId);
}
