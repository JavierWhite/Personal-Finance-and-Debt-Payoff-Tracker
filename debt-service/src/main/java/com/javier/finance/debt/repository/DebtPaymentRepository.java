package com.javier.finance.debt.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.javier.finance.debt.entity.DebtPayment;

public interface DebtPaymentRepository extends JpaRepository<DebtPayment, Long> {
}
