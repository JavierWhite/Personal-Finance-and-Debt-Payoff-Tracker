package com.javier.finance.retirement.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.javier.finance.retirement.entity.RetirementAccount;

public interface RetirementAccountRepository extends JpaRepository<RetirementAccount, Long> {
    List<RetirementAccount> findByUserIdOrderByCreatedDateDesc(Long userId);
}
