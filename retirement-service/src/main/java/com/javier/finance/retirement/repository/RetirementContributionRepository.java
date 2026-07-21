package com.javier.finance.retirement.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.javier.finance.retirement.entity.RetirementContribution;

public interface RetirementContributionRepository extends JpaRepository<RetirementContribution, Long> {
}
