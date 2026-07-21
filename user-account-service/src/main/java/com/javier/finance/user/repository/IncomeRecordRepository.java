package com.javier.finance.user.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.javier.finance.user.entity.IncomeRecord;

public interface IncomeRecordRepository extends JpaRepository<IncomeRecord, Long> {
    List<IncomeRecord> findByUserId(Long userId);
    List<IncomeRecord> findByUserIdOrderByCreatedDateDesc(Long userId);
    void deleteByUserId(Long userId);
}
