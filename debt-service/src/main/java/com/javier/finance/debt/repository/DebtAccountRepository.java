package com.javier.finance.debt.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.javier.finance.debt.entity.DebtAccount;

public interface DebtAccountRepository extends JpaRepository<DebtAccount, Long> {
    List<DebtAccount> findByUserIdOrderByCreatedDateDesc(Long userId);
}
