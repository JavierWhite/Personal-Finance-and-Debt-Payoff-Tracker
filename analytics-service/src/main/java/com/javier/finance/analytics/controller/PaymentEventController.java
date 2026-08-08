
package com.javier.finance.analytics.controller;

import java.util.List;
import java.util.Map;

import com.javier.finance.analytics.entity.ProcessedDebtPaymentEvent;
import com.javier.finance.analytics.repository.ProcessedDebtPaymentEventRepository;
import com.javier.finance.security.SecurityAccess;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/analytics/events/debt-payments")
public class PaymentEventController {
    private final ProcessedDebtPaymentEventRepository repository;

    public PaymentEventController(ProcessedDebtPaymentEventRepository repository) {
        this.repository = repository;
    }

    @GetMapping("/user/{userId}")
    public List<ProcessedDebtPaymentEvent> recentForUser(@PathVariable Long userId) {
        SecurityAccess.requireSelfOrAdmin(userId);
        return repository.findTop50ByUserIdOrderByProcessedAtDesc(userId);
    }

    @GetMapping("/user/{userId}/count")
    public Map<String, Object> countForUser(@PathVariable Long userId) {
        SecurityAccess.requireSelfOrAdmin(userId);
        return Map.of("userId", userId, "processedDebtPaymentEvents", repository.countByUserId(userId));
    }
}
