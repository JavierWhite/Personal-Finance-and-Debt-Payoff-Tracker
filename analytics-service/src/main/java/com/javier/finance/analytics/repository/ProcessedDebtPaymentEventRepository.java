
package com.javier.finance.analytics.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.javier.finance.analytics.entity.ProcessedDebtPaymentEvent;

public interface ProcessedDebtPaymentEventRepository extends JpaRepository<ProcessedDebtPaymentEvent, Long> {
    boolean existsByEventId(String eventId);
    List<ProcessedDebtPaymentEvent> findTop50ByUserIdOrderByProcessedAtDesc(Long userId);
    long countByUserId(Long userId);
}
