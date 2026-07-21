package com.javier.finance.analytics.controller;

import java.net.URI;
import java.util.List;

import com.javier.finance.security.SecurityAccess;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.javier.finance.analytics.dto.ChartDataPoint;
import com.javier.finance.analytics.dto.FinanceSummaryResponse;
import com.javier.finance.analytics.dto.MonthlyFinanceSnapshotRequest;
import com.javier.finance.analytics.entity.MonthlyFinanceSnapshot;
import com.javier.finance.analytics.service.AnalyticsService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/analytics")
public class AnalyticsController {
    private final AnalyticsService service;

    public AnalyticsController(AnalyticsService service) { this.service = service; }

    @PostMapping("/snapshots")
    public ResponseEntity<MonthlyFinanceSnapshot> create(@Valid @RequestBody MonthlyFinanceSnapshotRequest request) {
        SecurityAccess.requireSelfOrAdmin(request.userId());
        MonthlyFinanceSnapshot created = service.create(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")
                .buildAndExpand(created.getId()).toUri();
        return ResponseEntity.created(location).body(created);
    }

    @GetMapping("/snapshots/{id}")
    public MonthlyFinanceSnapshot findById(@PathVariable Long id) {
        MonthlyFinanceSnapshot snapshot = service.findById(id);
        SecurityAccess.requireSelfOrAdmin(snapshot.getUserId());
        return snapshot;
    }

    @GetMapping("/snapshots/user/{userId}")
    public List<MonthlyFinanceSnapshot> findByUser(@PathVariable Long userId) {
        SecurityAccess.requireSelfOrAdmin(userId);
        return service.findByUserId(userId);
    }

    @PutMapping("/snapshots/{id}")
    public MonthlyFinanceSnapshot update(@PathVariable Long id,
            @Valid @RequestBody MonthlyFinanceSnapshotRequest request) {
        MonthlyFinanceSnapshot snapshot = service.findById(id);
        SecurityAccess.requireSelfOrAdmin(snapshot.getUserId());
        SecurityAccess.requireSelfOrAdmin(request.userId());
        return service.update(id, request);
    }

    @DeleteMapping("/snapshots/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        MonthlyFinanceSnapshot snapshot = service.findById(id);
        SecurityAccess.requireSelfOrAdmin(snapshot.getUserId());
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/{userId}/summary")
    public FinanceSummaryResponse summary(@PathVariable Long userId) {
        SecurityAccess.requireSelfOrAdmin(userId);
        return service.summary(userId);
    }

    @GetMapping("/user/{userId}/debt-chart")
    public List<ChartDataPoint> debtChart(@PathVariable Long userId) {
        SecurityAccess.requireSelfOrAdmin(userId);
        return service.debtChart(userId);
    }

    @GetMapping("/user/{userId}/savings-chart")
    public List<ChartDataPoint> savingsChart(@PathVariable Long userId) {
        SecurityAccess.requireSelfOrAdmin(userId);
        return service.savingsChart(userId);
    }

    @GetMapping("/user/{userId}/net-worth-chart")
    public List<ChartDataPoint> netWorthChart(@PathVariable Long userId) {
        SecurityAccess.requireSelfOrAdmin(userId);
        return service.netWorthChart(userId);
    }
}
