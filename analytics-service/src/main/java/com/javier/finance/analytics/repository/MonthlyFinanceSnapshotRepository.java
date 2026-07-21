package com.javier.finance.analytics.repository;

import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.javier.finance.analytics.entity.MonthlyFinanceSnapshot;

public interface MonthlyFinanceSnapshotRepository extends JpaRepository<MonthlyFinanceSnapshot, Long> {
    List<MonthlyFinanceSnapshot> findByUserIdOrderByMonthAsc(Long userId);
    Optional<MonthlyFinanceSnapshot> findFirstByUserIdOrderByMonthDesc(Long userId);
    boolean existsByUserIdAndMonth(Long userId, YearMonth month);
    boolean existsByUserIdAndMonthAndIdNot(Long userId, YearMonth month, Long id);
}
