package com.javier.finance.savings.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.javier.finance.savings.entity.SavingsContribution;

public interface SavingsContributionRepository extends JpaRepository<SavingsContribution, Long> {
}
